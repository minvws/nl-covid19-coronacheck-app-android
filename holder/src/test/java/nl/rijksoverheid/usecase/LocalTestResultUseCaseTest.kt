package nl.rijksoverheid.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class LocalTestResultUseCaseTest {

    private val persistenceManager = mockk<PersistenceManager>(relaxUnitFun = true)
    private val testResultUtil = mockk<TestResultUtil>()
    private val testResultRepository = mockk<TestResultRepository>()
    private val localTestResultUseCase = LocalTestResultUseCase(
        persistenceManager = persistenceManager,
        testResultUtil = testResultUtil,
        testResultRepository = testResultRepository
    )

    @Test
    fun `Getting invalid test result returns no test result and clears local stored test result`() =
        runBlocking {
            val localTestResult = LocalTestResult(
                credentials = "",
                sampleDate = OffsetDateTime.now()
            )

            every { persistenceManager.getLocalTestResult() } answers { localTestResult }
            every { testResultUtil.isValid(any(), any(), any()) } answers { false }
            coEvery { testResultRepository.getTestValiditySeconds() } answers { 10 }

            val result = localTestResultUseCase.get(OffsetDateTime.now())

            verify(exactly = 1) { persistenceManager.deleteLocalTestResult() }
            assertNull(result)
        }

    @Test
    fun `Getting valid test result returns test result and does not clear local stored test result`() =
        runBlocking {
            val localTestResult = LocalTestResult(
                credentials = "",
                sampleDate = OffsetDateTime.now()
            )

            every { persistenceManager.getLocalTestResult() } answers { localTestResult }
            every { testResultUtil.isValid(any(), any(), any()) } answers { true }
            coEvery { testResultRepository.getTestValidity() } answers { 10 }

            val result = localTestResultUseCase.get(OffsetDateTime.now())

            verify(exactly = 0) { persistenceManager.deleteLocalTestResult() }
            assertEquals(result, localTestResult)
        }

}
