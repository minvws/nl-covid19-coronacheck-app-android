/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.api.interceptors

import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.ROOT_CA_G3
import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.signing.SignatureValidationException
import nl.rijksoverheid.ctr.signing.SignatureValidator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Invocation
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.nio.charset.CharacterCodingException

private val responseAdapter by lazy {
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Base64JsonAdapter())
        .build()
        .adapter(SignedResponse::class.java)
}

class SignedResponseInterceptor(
    signatureCertificateCnMatch: String,
    private val testProviderApiChecks: Boolean
) : Interceptor {
    private val defaultValidator = SignatureValidator.Builder()
        .addTrustedCertificate(EV_ROOT_CA)
        .cnMatching(signatureCertificateCnMatch)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val expectedSigningCertificate = chain.request().tag(SigningCertificate::class.java)
        val wrapResponse = expectedSigningCertificate != null

        val response = chain.proceed(chain.request())

        // if not marked with SignedRequest, return the response
        chain.request()
            .tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(SignedRequest::class.java) ?: return response

        val body = response.body?.bytes() ?: return response

        try {
            val signedResponse = responseAdapter.fromJson(Buffer().apply { write(body) })

            if (signedResponse?.payload == null || signedResponse.signature == null) {
                return response.newBuilder().body("Empty signature".toResponseBody())
                    .code(500)
                    .message("Expected response signature").build().also { response.close() }
            }

            val validator = if (expectedSigningCertificate != null) {
                val builder = SignatureValidator.Builder()
                    .addTrustedCertificate(EV_ROOT_CA)
                    .addTrustedCertificate(ROOT_CA_G3)
                    .addTrustedCertificate(PRIVATE_ROOT_CA)
                    .signingCertificate(expectedSigningCertificate.certificateBytes)
                builder.build()
            } else {
                defaultValidator
            }

            return if (!validateSignature(validator, signedResponse)) {
                throw CharacterCodingException()
            } else {
                response.newBuilder()
                    .body(
                        (if (wrapResponse) wrapResponse(
                            body,
                            signedResponse.payload
                        ) else signedResponse.payload).toResponseBody("application/json".toMediaType())
                    )
                    .build().also { response.close() }
            }
        } catch (e: Exception) {
            return if (response.isSuccessful) {
                throw CharacterCodingException()
            } else {
                // When something is wrong in parsing a unsuccessful request, cascade down the
                // request as usual (so that HttpExceptions get picked up for example)
                response.newBuilder().body(body.toResponseBody())
                    .code(response.code).build().also { response.close() }
            }
        }
    }

    private fun wrapResponse(signedBody: ByteArray, response: ByteArray): ByteArray {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer)
        writer.beginObject()
        writer.name("rawResponse")
        writer.value(Base64.encodeToString(signedBody, Base64.NO_WRAP))
        writer.name("model")
        writer.value(Buffer().apply { write(response) })
        writer.endObject()
        writer.flush()
        return buffer.readByteArray()
    }

    private fun validateSignature(
        validator: SignatureValidator,
        signedResponse: SignedResponse
    ): Boolean {
        if (signedResponse.signature == null || signedResponse.payload == null) {
            return false
        }
        return try {
            if (testProviderApiChecks) {
                validator.verifySignature(
                    ByteArrayInputStream(signedResponse.payload),
                    signedResponse.signature
                )
            }
            true
        } catch (ex: SignatureValidationException) {
            Timber.w(ex, "Error validating signature")
            false
        }
    }
}

@JsonClass(generateAdapter = true)
internal class SignedResponse(
    val payload: ByteArray?,
    val signature: ByteArray?
)

/**
 * Holder class for the signing certificate
 */
class SigningCertificate(val certificateBytes: ByteArray)
