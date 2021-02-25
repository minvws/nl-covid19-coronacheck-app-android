package nl.rijksoverheid.ctr.holder

import nl.rijksoverheid.ctr.api.models.*
import nl.rijksoverheid.ctr.api.repositories.TestResultRepository
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.usecase.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeCreateCredentialUseCase(
    credential: String = ""
): CreateCredentialUseCase {
    return object : CreateCredentialUseCase {
        override fun get(secretKeyJson: String, testIsmBody: String): String {
            return credential
        }
    }
}

fun fakeSecretKeyUseCase(
    json: String = "{}"
): SecretKeyUseCase {
    return object : SecretKeyUseCase {
        override fun json(): String {
            return json
        }

        override fun persist() {

        }
    }
}

fun fakeCommitmentMessageUsecase(
    json: String = "{}"
): CommitmentMessageUseCase {
    return object : CommitmentMessageUseCase {
        override suspend fun json(nonce: String): String {
            return json
        }
    }
}

fun fakeTestProviderRepository(
    model: SignedResponseWithModel<RemoteTestResult> = SignedResponseWithModel(
        rawResponse = "dummy".toByteArray(),
        model = RemoteTestResult(
            result = null,
            protocolVersion = "1",
            providerIdentifier = "1",
            status = RemoteTestResult.Status.COMPLETE
        ),
    ),
    remoteTestResultExceptionCallback: (() -> Unit)? = null,
): TestProviderRepository {
    return object : TestProviderRepository {
        override suspend fun remoteTestResult(
            url: String,
            token: String,
            verifierCode: String?,
            signingCertificateBytes: ByteArray
        ): SignedResponseWithModel<RemoteTestResult> {
            remoteTestResultExceptionCallback?.invoke()
            return model
        }
    }
}

fun fakeTestProviderUseCase(
    provider: RemoteTestProviders.Provider? = null
): TestProviderUseCase {
    return object : TestProviderUseCase {
        override suspend fun testProvider(id: String): RemoteTestProviders.Provider? {
            return provider
        }
    }
}

fun fakeCoronaCheckRepository(
    testProviders: RemoteTestProviders = RemoteTestProviders(listOf()),
    testIsmResult: TestIsmResult = TestIsmResult.Success(""),
    testIsmExceptionCallback: (() -> Unit)? = null,
    remoteNonce: RemoteNonce = RemoteNonce("", ""),

    ): CoronaCheckRepository {
    return object : CoronaCheckRepository {
        override suspend fun testProviders(): RemoteTestProviders {
            return testProviders
        }

        override suspend fun getTestIsm(test: String, sToken: String, icm: String): TestIsmResult {
            testIsmExceptionCallback?.invoke()
            return testIsmResult
        }

        override suspend fun remoteNonce(): RemoteNonce {
            return remoteNonce
        }
    }
}

fun fakeTestResultAttributesUseCase(
    sampleTimeSeconds: Long = 0L,
    testType: String = ""
): TestResultAttributesUseCase {
    return object : TestResultAttributesUseCase {
        override fun get(credentials: String): TestResultAttributes {
            return TestResultAttributes(
                sampleTime = sampleTimeSeconds,
                testType = ""
            )
        }
    }
}

fun fakeTestResultRepository(
    testValiditySeconds: Long = 0,
    issuerPublicKey: String = ""
): TestResultRepository {
    return object : TestResultRepository {
        override suspend fun getIssuerPublicKey(): String {
            return issuerPublicKey
        }

        override suspend fun getTestValiditySeconds(): Long {
            return testValiditySeconds
        }
    }
}

fun fakePersistenceManager(
    secretKeyJson: String? = "",
    credentials: String? = ""
): PersistenceManager {
    return object : PersistenceManager {
        override fun saveSecretKeyJson(json: String) {

        }

        override fun getSecretKeyJson(): String? {
            return secretKeyJson
        }

        override fun saveCredentials(credentials: String) {

        }

        override fun getCredentials(): String? {
            return credentials
        }

        override fun deleteCredentials() {

        }
    }
}


