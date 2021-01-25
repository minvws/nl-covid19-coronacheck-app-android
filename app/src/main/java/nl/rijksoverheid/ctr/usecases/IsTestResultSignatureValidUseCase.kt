package nl.rijksoverheid.ctr.usecases

import nl.rijksoverheid.ctr.data.models.Issuers
import nl.rijksoverheid.ctr.data.models.TestResults

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IsTestResultSignatureValidUseCase(private val isSignatureValidUseCase: IsSignatureValidUseCase) {

    sealed class IsTestResultValidResult {
        object Valid : IsTestResultValidResult()
        class Invalid(val reason: String) : IsTestResultValidResult()
    }

    fun isValid(
        issuers: List<Issuers.Issuer>,
        validTestResultForEvent: TestResults.TestResult,
        validTestResultSignature: String
    ): IsTestResultValidResult {
        val validTestResultSignatureValid = isSignatureValidUseCase.isValid(
            issuers,
            validTestResultSignature,
            validTestResultForEvent
        )
        return if (validTestResultSignatureValid) {
            IsTestResultValidResult.Valid
        } else {
            IsTestResultValidResult.Invalid("Test Result Signature Invalid")
        }
    }

}
