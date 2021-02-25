package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCaseTest {

    @Test
    fun `testResult returns InvalidToken if a uniquecode does not contain -`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase()
        )
        val result = usecase.testResult(uniqueCode = "dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

}
