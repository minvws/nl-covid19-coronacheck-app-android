package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class LocalTestResultUseCase(
    private val persistenceManager: PersistenceManager,
    private val testResultUtil: TestResultUtil,
    private val testResultRepository: TestResultRepository
) {

    suspend fun get(currentDate: OffsetDateTime): LocalTestResult? {
        val localTestResult = persistenceManager.getLocalTestResult()
        localTestResult?.let { localTestResult ->
            val isValid = testResultUtil.isValid(
                currentDate = currentDate,
                sampleDate = localTestResult.sampleDate,
                validitySeconds = testResultRepository.getTestValidity()
            )
            return if (isValid) {
                localTestResult
            } else {
                persistenceManager.deleteLocalTestResult()
                return null
            }
        }
        return null
    }

    fun save(credentials: String, sampleDate: OffsetDateTime) {
        val localTestResult = LocalTestResult(
            credentials = credentials,
            sampleDate = sampleDate
        )
        persistenceManager.saveLocalTestResult(localTestResult)
    }

}
