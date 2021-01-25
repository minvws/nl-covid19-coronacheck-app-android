package nl.rijksoverheid.ctr.usecases

import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.data.models.EventQr
import nl.rijksoverheid.ctr.data.models.TestResults

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetValidTestResultForEventUseCase(
    private val eventUtil: EventUtil
) {

    sealed class TestResultValidResult {
        class Valid(
            val testResult: TestResults.TestResult,
            val testResultSignature: String
        ) : TestResultValidResult()

        class Invalid(val reason: String) : TestResultValidResult()
    }

    fun isValid(
        event: EventQr.Event,
        testResults: TestResults
    ): TestResultValidResult {
        val validTestResultForEvent = eventUtil.getValidTestResult(
            event = event,
            userTestResults = testResults.testResults
        ) ?: return TestResultValidResult.Invalid("No valid test found")

        val validTestResultSignature =
            testResults.testSignatures.first { it.uuid == validTestResultForEvent.uuid }.signature

        return TestResultValidResult.Valid(validTestResultForEvent, validTestResultSignature)
    }

}
