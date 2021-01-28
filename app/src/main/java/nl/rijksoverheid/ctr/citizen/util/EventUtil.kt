package nl.rijksoverheid.ctr.citizen.util

import nl.rijksoverheid.ctr.shared.models.Agent
import nl.rijksoverheid.ctr.shared.models.Event
import nl.rijksoverheid.ctr.shared.models.TestResults
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class EventUtil {

    fun timeValid(event: Event): Boolean {
        return event.validFrom <= OffsetDateTime.now()
            .toEpochSecond() && event.validTo >= OffsetDateTime.now().toEpochSecond()
    }

    fun allowedTestResult(
        event: Event,
        userTestResults: List<TestResults.TestResult>
    ): TestResults.TestResult? {
        event.validTests.forEach { validTestForEvent ->
            userTestResults.forEach { userTestResult ->
                if (validTestForEvent.uuid == userTestResult.testType && validTestForEvent.maxValdity + userTestResult.dateTaken >= OffsetDateTime.now()
                        .toEpochSecond()
                ) {
                    return userTestResult
                }
            }
        }
        return null
    }

    fun allowedTestResult(
        event: Agent.Event,
        userTestResult: TestResults.TestResult
    ): Boolean {
        event.validTests.forEach { validTestForEvent ->
            return validTestForEvent.uuid == userTestResult.testType && validTestForEvent.maxValidity + userTestResult.dateTaken >= OffsetDateTime.now()
                .toEpochSecond() && userTestResult.result == 0
        }
        return false
    }
}
