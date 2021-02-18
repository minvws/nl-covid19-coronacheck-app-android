package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
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

    suspend fun get(): LocalTestResultState = withContext(Dispatchers.IO) {
        val credentials = persistenceManager.getCredentials()
        if (credentials != null) {
            val testAttributes = testResultAttributesUseCase.get(credentials)
            val sampleDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(testAttributes.sampleTime),
                ZoneOffset.UTC
            )
            val testValiditySeconds = testResultRepository.getTestValiditySeconds()

            val isValid = testResultUtil.isValid(
                sampleDate = sampleDate,
                validitySeconds = testValiditySeconds
            )

            if (isValid) {
                LocalTestResultState.Valid(LocalTestResult(
                    credentials = credentials,
                    sampleDate = sampleDate,
                    testType = testAttributes.testType,
                    expireDate = sampleDate.plusSeconds(testValiditySeconds)
                ))
            } else {
                persistenceManager.deleteCredentials()
                LocalTestResultState.Expired
            }
        } else {
            LocalTestResultState.None
        }
    }
}
