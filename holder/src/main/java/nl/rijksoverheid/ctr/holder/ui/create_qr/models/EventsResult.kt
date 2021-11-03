package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.exceptions.NoProvidersException
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

sealed class EventsResult {
    data class Success (
        val signedModels: List<SignedResponseWithModel<RemoteProtocol3>>,
        val missingEvents: Boolean,
        val eventProviders: List<EventProvider>,
    ) :
        EventsResult()

    data class HasNoEvents(val missingEvents: Boolean, val errorResults: List<ErrorResult> = emptyList()) : EventsResult()

    data class CannotCreateRecovery(val validityDays: Int): EventsResult()

    data class Error constructor(val errorResults: List<ErrorResult>): EventsResult() {
        constructor(errorResult: ErrorResult): this(listOf(errorResult))

        fun accessTokenSessionExpiredError(): Boolean {
            val accessTokenCallError = errorResults.find { it.getCurrentStep() == HolderStep.AccessTokensNetworkRequest }
            accessTokenCallError?.let {
                return hasErrorCode(it, 99708)
            }
            return false
        }

        fun accessTokenNoBsn(): Boolean {
            val accessTokenCallError = errorResults.find { it.getCurrentStep() == HolderStep.AccessTokensNetworkRequest }
            accessTokenCallError?.let {
                return hasErrorCode(it, 99782)
            }
            return false
        }

        private fun hasErrorCode(errorResult: ErrorResult, expectedErrorCode: Int): Boolean {
            return if (errorResult is NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError) {
                errorResult.getCode() == expectedErrorCode
            } else {
                false
            }
        }

        fun unomiOrEventErrors(): Boolean {
            val unomiOrEventErrors = errorResults.find { it.getCurrentStep() == HolderStep.UnomiNetworkRequest || it.getCurrentStep() == HolderStep.EventNetworkRequest }
            return unomiOrEventErrors != null
        }

        companion object {
            fun noProvidersError(originType: OriginType) = Error(object: ErrorResult {
                override fun getCurrentStep() = HolderStep.ConfigProvidersNetworkRequest

                override fun getException() = when (originType) {
                    OriginType.Recovery -> NoProvidersException.Recovery
                    OriginType.Test -> NoProvidersException.Test
                    OriginType.Vaccination -> NoProvidersException.Vaccination
                }
            })
        }
    }
}
