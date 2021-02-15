package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
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
    private val moshi: Moshi
) {

    suspend fun get(currentDate: OffsetDateTime): LocalTestResult? {
        val localTestResult = persistenceManager.getLocalTestResult()
        localTestResult?.let { localTestResult ->
            val result = Clmobile.readCredential(localTestResult.credentials.toByteArray()).verify()
            val testAttributes = result.decodeToString().toObject<TestResultAttributes>(moshi)

            val isValid = testResultUtil.isValid(
                currentDate = currentDate,
                sampleDate = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(testAttributes.sampleTime),
                    ZoneOffset.UTC
                ),
                validitySeconds = testResultRepository.getTestValiditySeconds()
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

}
