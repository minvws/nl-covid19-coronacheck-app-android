package nl.rijksoverheid.ctr.holder

import nl.rijksoverheid.ctr.api.models.RemoteNonce
import nl.rijksoverheid.ctr.api.models.RemoteTestProviders
import nl.rijksoverheid.ctr.api.models.TestIsmResult
import nl.rijksoverheid.ctr.api.models.TestResultAttributes
import nl.rijksoverheid.ctr.api.repositories.TestResultRepository
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.usecase.TestResultAttributesUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeCoronaCheckRepository(
    testProviders: RemoteTestProviders = RemoteTestProviders(listOf()),
    testIsmResult: TestIsmResult = TestIsmResult.Success(""),
    remoteNonce: RemoteNonce = RemoteNonce("", "")
): CoronaCheckRepository {
    return object : CoronaCheckRepository {
        override suspend fun testProviders(): RemoteTestProviders {
            return testProviders
        }

        override suspend fun getTestIsm(test: String, sToken: String, icm: String): TestIsmResult {
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


