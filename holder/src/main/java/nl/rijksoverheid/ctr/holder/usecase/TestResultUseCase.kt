package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCase(
    private val testProviderUseCase: TestProviderUseCase,
    private val holderRepository: HolderRepository,
    private val moshi: Moshi,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val secretKeyUseCase: SecretKeyUseCase,
    private val persistenceManager: PersistenceManager
) {

    suspend fun testResult(uniqueCode: String, verificationCode: String): RemoteTestResult {
        val uniqueCodeAttributes = uniqueCode.split("-")
        val providerIdentifier = uniqueCodeAttributes[0]
        val token = uniqueCodeAttributes[1]

        val testProvider = testProviderUseCase.testProvider(providerIdentifier)
            ?: throw Exception("Unknown test provider") // TODO: Catch exception

        val remoteTestResult = holderRepository.remoteTestResult(
            url = testProvider.resultUrl,
            token = token,
            verifierCode = verificationCode,
            signingCertificateBytes = testProvider.publicKey
        )

        // Persist encrypted test result
        val remoteNonce = holderRepository.remoteNonce()
        val commitmentMessage = commitmentMessageUseCase.json(
            nonce =
            remoteNonce.nonce
        )
        Timber.i("Received commitment message $commitmentMessage")

        val testIsmJson = holderRepository.testIsmJson(
            test = remoteTestResult.toJson(moshi),
            sToken = remoteNonce.sToken,
            icm = commitmentMessage
        )
        Timber.i("Received test ism json $testIsmJson")

        val credentials = Clmobile.createCredential(
            secretKeyUseCase.json().toByteArray(),
            testIsmJson.toByteArray()
        ).successString()

        val localTestResult = LocalTestResult(
            credentials = credentials,
            sampleDate = remoteTestResult.result.sampleDate
        )

        persistenceManager.saveLocalTestResult(localTestResult)

        return remoteTestResult
    }
}
