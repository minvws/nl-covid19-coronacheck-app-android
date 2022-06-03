/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.api.signing

import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cms.CMSSignedDataParser
import org.bouncycastle.cms.CMSTypedStream
import org.bouncycastle.cms.SignerId
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.cert.*
import java.time.Clock


class SignatureValidator private constructor(
    private val signingCertificates: List<X509Certificate>,
    private val trustAnchors: Set<TrustAnchor>,
    private val matchingString: String?,
) {

    class Builder {
        private var trustAnchors = mutableSetOf<TrustAnchor>()
        private var signingCertificates: List<X509Certificate> = emptyList()
        private var matchingString: String? = null

        /**
         * The subject key identifier of the root certificate that is used as the trust anchor. If unset the default value will be used.
         */
        fun addTrustedCertificate(certificate: X509Certificate): Builder {
            trustAnchors.add(TrustAnchor(certificate, null))
            return this
        }

        fun addTrustedCertificate(certificatePem: String): Builder {
            val factory = CertificateFactory.getInstance("X509")
            return addTrustedCertificate(
                factory.generateCertificate(
                    ByteArrayInputStream(
                        certificatePem.toByteArray()
                    )
                ) as X509Certificate
            )
        }

        /**
         * The signing certificate that needs to be for the signature. If set, the signing certificate needs to match this certificate
         * in order to pass the signature validation.
         */
        fun signingCertificate(signingCertificates: List<X509Certificate>): Builder {
            this.signingCertificates = signingCertificates
            this.matchingString = null
            return this
        }

        fun signingCertificateBytes(signingCertificateBytes: List<ByteArray>): Builder {
            val x509s = signingCertificateBytes.map {
                CertificateFactory.getInstance("X509")
                    .generateCertificate(ByteArrayInputStream(it)) as X509Certificate
            }
            signingCertificate(
                x509s
            )
            return this
        }

        /**
         * Set a substring that the CN of the signing certificate should match. Not used when [signingCertificate] is set.
         */
        fun cnMatching(substring: String): Builder {
            if (signingCertificates.isNotEmpty()) {
                throw IllegalStateException("CN matching string cannot be used if signing certificate is set")
            }
            this.matchingString = substring
            return this
        }

        fun build(): SignatureValidator {
            return SignatureValidator(
                signingCertificates,
                trustAnchors,
                matchingString
            )
        }
    }

    private val provider = BouncyCastleProvider()

    fun verifySignature(
        content: InputStream,
        signature: ByteArray,
        clock: Clock = Clock.systemUTC()
    ) {

        try {
            val sp = CMSSignedDataParser(
                JcaDigestCalculatorProviderBuilder().setProvider(provider)
                    .build(),
                CMSTypedStream(BufferedInputStream(content)), signature
            )

            sp.signedContent.drain()

            val certs = sp.certificates

            val store: CertStore =
                JcaCertStoreBuilder().setProvider(provider)
                    .apply {
                        for (anchor in trustAnchors) {
                            addCertificate(JcaX509CertificateHolder(anchor.trustedCert))
                        }
                    }
                    .addCertificates(certs)
                    .build()

            val signer =
                sp.signerInfos.signers.firstOrNull()
                    ?: throw SignatureValidationException("No signing certificate found")

            if (this.signingCertificates.isNotEmpty()) {
                val nowMs = clock.millis()
                if (this.signingCertificates.all {
                        val expiringTime = it.notAfter.time
                        nowMs > expiringTime
                    }) {
                    throw SignatureValidationException("Expired certificate")
                }
            }

            val result = checkCertPath(trustAnchors, signer.sid, store)
            val signingCertificate = result.certPath.certificates[0] as X509Certificate

            if (this.signingCertificates.isNotEmpty() && this.signingCertificates.all { it != signingCertificate }) {
                throw SignatureValidationException("Signing certificate does not match expected certificate")
            }

            if (matchingString != null) {
                val subjectRDNs =
                    JcaX509CertificateHolder(signingCertificate).subject.getRDNs(BCStyle.CN)
                        .map { IETFUtils.valueToString(it.first.value) }
                if (!subjectRDNs.any {
                        it.endsWith(matchingString)
                    }) {
                    throw SignatureValidationException("Signing certificate does not match expected CN")
                }
            }

            if (!signer.verify(
                    JcaSimpleSignerInfoVerifierBuilder().setProvider(provider)
                        .build(signingCertificate)
                )
            ) {
                throw SignatureValidationException("The signature does not match")
            }
        } catch (ex: CertPathBuilderException) {
            throw SignatureValidationException("The cert path cannot be validated")
        } catch (ex: SignatureValidationException) {
            throw ex
        } catch (ex: Exception) {
            throw SignatureValidationException("Error validating signature", ex)
        }
    }

    private fun checkCertPath(
        trustAnchors: Set<TrustAnchor>,
        signerId: SignerId,
        certs: CertStore
    ): PKIXCertPathBuilderResult {
        val pathBuilder: CertPathBuilder =
            CertPathBuilder.getInstance("PKIX", provider)
        val targetConstraints = X509CertSelector()

        // criteria to target the certificate to build the path to:
        // must match the signing certificate that we pass in, and the
        // signing certificate must have the correct authority key identifier, if one is configured
        targetConstraints.setIssuer(signerId.issuer.encoded)
        targetConstraints.serialNumber = signerId.serialNumber

        val params = PKIXBuilderParameters(
            trustAnchors,
            targetConstraints
        )
        params.addCertStore(certs)
        params.isRevocationEnabled = false
        return pathBuilder.build(params) as PKIXCertPathBuilderResult
    }
}

class SignatureValidationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
