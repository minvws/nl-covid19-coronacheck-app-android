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
import java.io.ByteArrayInputStream
import java.security.SignatureException
import java.time.Clock
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.ROOT_CA_G3
import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.rdo.modules.httpsecurity.cms.CMSSignatureValidatorBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Invocation

private val responseAdapter by lazy {
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Base64JsonAdapter())
        .build()
        .adapter(SignedResponse::class.java)
}

class SignedResponseInterceptor(
    signatureCertificateCnMatch: String,
    private val testProviderApiChecks: Boolean,
    private val isAcc: Boolean,
    private val clock: Clock
) : Interceptor {
    private val defaultValidator = CMSSignatureValidatorBuilder.build(
        certificatesPem = listOf(PRIVATE_ROOT_CA),
        cnMatchingString = signatureCertificateCnMatch,
        clock = clock
    )

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
                val trustedCertificates = listOf(ROOT_CA_G3, PRIVATE_ROOT_CA)
                CMSSignatureValidatorBuilder.build(
                    certificatesPem = trustedCertificates,
                    signingCertificateBytes = expectedSigningCertificate.certificateBytes,
                    clock = clock
                )
            } else {
                defaultValidator
            }

            if (testProviderApiChecks) {
                validator.validate(
                    signature = signedResponse.signature,
                    content = ByteArrayInputStream(signedResponse.payload)
                )
            }

            return response.newBuilder()
                .body(
                    (if (wrapResponse) wrapResponse(
                        body,
                        signedResponse.payload
                    ) else signedResponse.payload).toResponseBody("application/json".toMediaType())
                )
                .build().also { response.close() }
        } catch (e: Exception) {
            return if (response.isSuccessful) {
                throw SignatureException("Invalid signature")
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
}

@JsonClass(generateAdapter = true)
internal class SignedResponse(
    val payload: ByteArray?,
    val signature: ByteArray?
)

/**
 * Holder class for the signing certificate
 */
class SigningCertificate(val certificateBytes: List<ByteArray>)
