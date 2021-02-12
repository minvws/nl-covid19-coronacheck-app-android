/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.signing

import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cms.CMSSignedDataParser
import org.bouncycastle.cms.CMSTypedStream
import org.bouncycastle.cms.SignerId
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.util.encoders.Hex
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertPathBuilder
import java.security.cert.CertPathBuilderException
import java.security.cert.CertStore
import java.security.cert.CertificateFactory
import java.security.cert.PKIXBuilderParameters
import java.security.cert.PKIXCertPathBuilderResult
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

// The publicly known default SubjectKeyIdentifier for the root PKI-O CA, retrieved from the device trust store
private val DEFAULT_ANCHOR_SUBJECT_KEY_IDENTIFIER =
    SubjectKeyIdentifier(Hex.decode("0414feab0090989e24fca9cc1a8afb27b8bf306ea83b"))

// The publicly known default AuthorityKeyIdentifier for the issuer that issued the signing certificate
private val DEFAULT_AUTHORITY_KEY_IDENTIFIER =
    AuthorityKeyIdentifier(Hex.decode("30168014084aaabb99246fbe5b07f1a58a995b2d47efb93c"))

class SignatureValidator private constructor(
    private val signingCertificate: X509Certificate?,
    trustManager: X509TrustManager,
    trustAnchorSubjectKeyIdentifier: SubjectKeyIdentifier,
    private val authorityKeyIdentifier: AuthorityKeyIdentifier?,
    private val cnMatchingRegex: Regex?
) {

    class Builder {
        private var cnMatchingRegEx: Regex? = null
        private var trustManager: X509TrustManager? = null
        private var trustAnchorSubjectKeyIdentifier: SubjectKeyIdentifier? = null
        private var signingCertificate: X509Certificate? = null
        private var authorityKeyIdentifier: AuthorityKeyIdentifier? = null

        /**
         * Set the trust manager to be used. If unset, the default trust manager will be used when calling [build]
         */
        fun trustManager(trustManager: X509TrustManager): Builder {
            this.trustManager = trustManager
            return this
        }

        /**
         * The subject key identifier of the root certificate that is used as the trust anchor. If unset the default value will be used.
         */
        fun trustAnchorSubjectKeyIdentifier(subjectKeyIdentifier: SubjectKeyIdentifier): Builder {
            this.trustAnchorSubjectKeyIdentifier = subjectKeyIdentifier
            return this
        }

        /**
         * The signing certificate that needs to be for the signature. If set, the signing certificate needs to match this certificate
         * in order to pass the signature validation. When signing certificate is set, [authorityKeyIdentifier] cannot be set.
         */
        fun signingCertificate(signingCertificate: X509Certificate): Builder {
            if (authorityKeyIdentifier != null) {
                throw IllegalStateException("Only one of authorityKeyIdentifier or signingCertificate can be set")
            }
            this.signingCertificate = signingCertificate
            this.cnMatchingRegEx = null
            return this
        }

        fun signingCertificate(signingCertificateBytes: ByteArray): Builder {
            signingCertificate(
                CertificateFactory.getInstance("X509")
                    .generateCertificate(ByteArrayInputStream(signingCertificateBytes)) as X509Certificate
            )
            return this
        }

        /**
         * The authority key identifier that the signing certificate needs to be signed with. This is used to check if
         * the signing certificated is issued by the expected issuer. The authority key identifier cannot be set if [signingCertificate] is set.
         */
        fun authorityKeyIdentifier(authorityKeyIdentifier: AuthorityKeyIdentifier?): Builder {
            if (signingCertificate != null) {
                throw IllegalStateException("Only one of authorityKeyIdentifier or signingCertificate can be set")
            }
            this.authorityKeyIdentifier = authorityKeyIdentifier
            return this
        }

        /**
         * Set the regex for validating the signing certificate CN. Not used when [signingCertificate] is set.
         */
        fun cnMatching(regex: Regex): Builder {
            if (signingCertificate != null) {
                throw IllegalStateException("CN regex cannot be used if signing certificate is set")
            }
            this.cnMatchingRegEx = regex
            return this
        }

        /**
         * Set a substring that the CN of the signing certificate should match.
         */
        fun cnMatching(substring: String): Builder {
            return cnMatching(Regex(Regex.escape(substring)))
        }

        fun build(): SignatureValidator {
            return SignatureValidator(
                signingCertificate,
                trustManager ?: getDefaultTrustManager(),
                trustAnchorSubjectKeyIdentifier ?: DEFAULT_ANCHOR_SUBJECT_KEY_IDENTIFIER,
                if (signingCertificate == null) authorityKeyIdentifier
                    ?: DEFAULT_AUTHORITY_KEY_IDENTIFIER else null,
                cnMatchingRegEx
            )
        }
    }

    private val trustAnchor: TrustAnchor?
    private val provider = BouncyCastleProvider()

    init {
        trustAnchor = getCertificateForSubjectKeyIdentifier(
            trustManager,
            trustAnchorSubjectKeyIdentifier
        )?.let {
            TrustAnchor(it, null)
        }
    }

    private fun getCertificateForSubjectKeyIdentifier(
        trustManager: X509TrustManager,
        subjectKeyIdentifier: SubjectKeyIdentifier
    ): X509Certificate? {
        return trustManager.acceptedIssuers.firstOrNull { certificate ->
            val ski = certificate.getExtensionValue(Extension.subjectKeyIdentifier.id)
                ?.let { SubjectKeyIdentifier.getInstance(it)?.keyIdentifier }
            ski?.contentEquals(subjectKeyIdentifier.keyIdentifier) == true
        }
    }

    fun verifySignature(content: InputStream, signature: ByteArray) {
        val trustAnchor = this.trustAnchor
            ?: throw SignatureValidationException("The trust anchor cannot be resolved")

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
                    .addCertificate(JcaX509CertificateHolder(trustAnchor.trustedCert))
                    .addCertificates(certs)
                    .build()

            val signer =
                sp.signerInfos.signers.firstOrNull()
                    ?: throw SignatureValidationException("No signing certificate found")
            val result = checkCertPath(trustAnchor, signer.sid, store)
            val signingCertificate = result.certPath.certificates[0] as X509Certificate

            if (this.signingCertificate != null && this.signingCertificate != signingCertificate) {
                throw SignatureValidationException("Signing certificate does not match expected certificate")
            }

            if (cnMatchingRegex != null) {
                if (!JcaX509CertificateHolder(signingCertificate).subject.getRDNs(BCStyle.CN).any {
                        val cn = IETFUtils.valueToString(it.first.value)
                        cnMatchingRegex.containsMatchIn(cn)
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
        trustAnchor: TrustAnchor,
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

        authorityKeyIdentifier?.let {
            targetConstraints.authorityKeyIdentifier = it.keyIdentifier
        }

        val params = PKIXBuilderParameters(
            setOf(trustAnchor),
            targetConstraints
        )

        params.addCertStore(certs)
        params.isRevocationEnabled = false
        return pathBuilder.build(params) as PKIXCertPathBuilderResult
    }
}

private fun getDefaultTrustManager(): X509TrustManager {
    val algorithm = TrustManagerFactory.getDefaultAlgorithm()
    val tm = TrustManagerFactory.getInstance(algorithm)
    @Suppress("CAST_NEVER_SUCCEEDS")
    tm.init(null as? KeyStore)
    return tm.trustManagers[0] as X509TrustManager
}

class SignatureValidationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
