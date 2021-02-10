package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCase(private val testProviderUseCase: TestProviderUseCase, private val holderRepository: HolderRepository) {

    suspend fun testResult(uniqueCode: String, verificationCode: String): RemoteTestResult {
        val uniqueCodeAttributes = uniqueCode.split("-")
        val providerIdentifier = uniqueCodeAttributes[0]
        val token = uniqueCodeAttributes[1]

        val testProvider = testProviderUseCase.testProvider(providerIdentifier) ?: throw Exception("Unknown test provider") // TODO: Catch exception
        return holderRepository.remoteTestResult(
            url = testProvider.resultUrl,
            token = token,
            verifierCode = verificationCode
        )
    }
}
