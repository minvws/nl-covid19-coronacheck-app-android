/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.api

import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.signing.SignatureValidationException
import nl.rijksoverheid.ctr.signing.SignatureValidator
import nl.rijksoverheid.ctr.signing.certificates.EV_ROOT_CA
import nl.rijksoverheid.ctr.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.signing.certificates.ROOT_CA_G3
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Invocation
import timber.log.Timber
import java.io.ByteArrayInputStream

private val responseAdapter by lazy {
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Base64JsonAdapter())
        .build()
        .adapter(SignedResponse::class.java)
}

class SignedResponseInterceptor : Interceptor {
    private val defaultValidator = SignatureValidator.Builder()
        .addTrustedCertificate(EV_ROOT_CA)
        .cnMatching(BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH)
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

        if (response.code !in 200..299 && response.code !in 400..499) {
            return response
        }

        val body = response.body?.bytes() ?: return response

        val signedResponse = responseAdapter.fromJson(Buffer().apply { write(body) })
            ?: error("Expected signed response payload")

        if (signedResponse.payload == null || signedResponse.signature == null) {
            return response.newBuilder().body("Empty signature".toResponseBody())
                .code(500)
                .message("Expected response signature").build().also { response.close() }
        }

        val validator = if (expectedSigningCertificate != null) {
            SignatureValidator.Builder()
                .addTrustedCertificate(EV_ROOT_CA)
                .addTrustedCertificate(ROOT_CA_G3)
                .addTrustedCertificate(PRIVATE_ROOT_CA)
                .signingCertificate(expectedSigningCertificate.certificateBytes).build()
        } else {
            defaultValidator
        }

        return if (!validateSignature(validator, signedResponse)) {
            response.newBuilder().body("Signature failed to validate".toResponseBody())
                .code(500)
                .message("Signature failed to validate").build().also { response.close() }
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
            validator.verifySignature(
                ByteArrayInputStream(signedResponse.payload),
                signedResponse.signature
            )
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
