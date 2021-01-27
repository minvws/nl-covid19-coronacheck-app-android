package nl.rijksoverheid.ctr.citizen.usecases

import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.shared.models.AllowedTestResult
import nl.rijksoverheid.ctr.shared.models.Event
import nl.rijksoverheid.ctr.shared.models.TestResults

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AllowedTestResultForEventUseCase(
    private val eventUtil: EventUtil
) {

    fun get(
        event: Event,
        testResults: TestResults
    ): AllowedTestResult {
        val allowedTestResultForEvent = eventUtil.allowedTestResult(
            event = event,
            userTestResults = testResults.testResults
        ) ?: throw Exception("No valid test found")

        val allowedTestResultSignature =
            testResults.testSignatures.first { it.uuid == allowedTestResultForEvent.uuid }

        return AllowedTestResult(
            testResult = allowedTestResultForEvent,
            testResultSignature = allowedTestResultSignature
        )
    }
}
