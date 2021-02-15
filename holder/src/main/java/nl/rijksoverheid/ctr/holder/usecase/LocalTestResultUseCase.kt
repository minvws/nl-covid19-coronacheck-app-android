package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
    private val testResultRepository: TestResultRepository,
    private val testResultAttributesUseCase: TestResultAttributesUseCase
) {

    suspend fun get(currentDate: OffsetDateTime): LocalTestResult? {
        val credentials = persistenceManager.getCredentials()
        credentials?.let { localTestResult ->
            val testAttributes = testResultAttributesUseCase.get(credentials)
            val sampleDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(testAttributes.sampleTime),
                ZoneOffset.UTC
            )

            val isValid = testResultUtil.isValid(
                currentDate = currentDate,
                sampleDate = sampleDate,
                validitySeconds = testResultRepository.getTestValiditySeconds()
            )
            return if (isValid) {
                LocalTestResult(
                    credentials = credentials,
                    sampleDate = sampleDate,
                    testType = testAttributes.testType
                )
            } else {
                persistenceManager.deleteCredentials()
                return null
            }
        }
        return null
    }

}
