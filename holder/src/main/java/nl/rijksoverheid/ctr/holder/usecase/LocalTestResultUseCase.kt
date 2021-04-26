package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.ClmobileVerifyException
import nl.rijksoverheid.ctr.shared.usecase.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface LocalTestResultUseCase {
    suspend fun get(currentLocalTestResultState: LocalTestResultState?): LocalTestResultState
}

open class LocalTestResultUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val testResultUtil: TestResultUtil,
    private val testResultAttributesUseCase: TestResultAttributesUseCase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val personalDetailsUtil: PersonalDetailsUtil
) : LocalTestResultUseCase {

    override suspend fun get(currentLocalTestResultState: LocalTestResultState?): LocalTestResultState =
        withContext(Dispatchers.IO) {
            val credentials = persistenceManager.getCredentials()
            if (credentials != null) {
                try {
                    val testAttributes = testResultAttributesUseCase.get(credentials)
                    val sampleDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(testAttributes.sampleTime),
                        ZoneOffset.UTC
                    )
                    val testValiditySeconds =
                        TimeUnit.HOURS.toSeconds(
                            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                        )

                    val isValid = testResultUtil.isValid(
                        sampleDate = sampleDate,
                        validitySeconds = testValiditySeconds
                    )

                    if (isValid) {
                        val personalDetails = personalDetailsUtil.getPersonalDetails(
                            firstNameInitial = testAttributes.firstNameInitial,
                            lastNameInitial = testAttributes.lastNameInitial,
                            birthDay = testAttributes.birthDay,
                            birthMonth = testAttributes.birthMonth,
                            includeBirthMonthNumber = false
                        )

                        // First time created if previous state is null (app first launch) and current state is different than previous state is different
                        val firstTimeCreated =
                            currentLocalTestResultState != null && currentLocalTestResultState !is LocalTestResultState.Valid

                        LocalTestResultState.Valid(
                            LocalTestResult(
                                credentials = credentials,
                                sampleDate = sampleDate,
                                testType = testAttributes.testType,
                                expireDate = sampleDate.plusSeconds(testValiditySeconds),
                                personalDetails = personalDetails
                            ),
                            firstTimeCreated = firstTimeCreated
                        )
                    } else {
                        persistenceManager.deleteCredentials()
                        LocalTestResultState.Expired
                    }
                } catch (e: Exception) {
                    LocalTestResultState.None
                }
            } else {
                LocalTestResultState.None
            }
        }
}
