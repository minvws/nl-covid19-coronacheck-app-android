/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.shared.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.shared.BuildConfig
import nl.rijksoverheid.ctr.shared.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.signing.SignatureValidationException
import nl.rijksoverheid.ctr.signing.SignatureValidator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
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
        .cnMatching(BuildConfig.SIGNATURE_CERTIFICATE_CN_MATCH)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val expectedSigningCertificate = chain.request().tag(SigningCertificate::class.java)

        val response = chain.proceed(
            chain.request().newBuilder()
                .url(
                    chain.request().url.newBuilder()
                        .apply {
                            // TODO remove when this is the default
                            query("sigInlineV2")
                        }.build()
                ).build()
        )

        val body = response.body ?: return response

        val signedResponse = body.use {
            responseAdapter.fromJson(it.source())
        } ?: error("Expected signed response payload")

        val validator = if (expectedSigningCertificate != null) {
            SignatureValidator.Builder()
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
                .body(signedResponse.payload.toResponseBody("application/json".toMediaType()))
                .build().also { response.close() }
        }
    }

    private fun validateSignature(
        validator: SignatureValidator,
        signedResponse: SignedResponse
    ): Boolean {
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
    val payload: ByteArray,
    val signature: ByteArray
)

/**
 * Holder class for the signing certificate
 */
class SigningCertificate(val certificateBytes: ByteArray)