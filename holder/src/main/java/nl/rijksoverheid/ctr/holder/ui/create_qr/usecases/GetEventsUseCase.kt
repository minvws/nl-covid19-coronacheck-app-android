package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Get events for a specific [OriginType]
 * This is the entry point class for getting Events and will take care of:
 * - getting all event providers
 * - getting tokens based on JWT
 * - getting events at event providers
 * - map result to success or error states
 */
interface GetEventsUseCase {
    suspend fun getEvents(jwt: String,
                          originType: OriginType,
                          targetProviderIds: List<String>? = null): EventsResult
}

class GetEventsUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase,
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase
) : GetEventsUseCase {

    override suspend fun getEvents(
        jwt: String,
        originType: OriginType,
        targetProviderIds: List<String>?
    ): EventsResult {
        // Fetch event providers
        val eventProvidersResult = configProvidersUseCase.eventProviders()
        val tokens = when (eventProvidersResult) {
            is EventProvidersResult.Error -> return EventsResult.Error(eventProvidersResult.errorResult)
            is EventProvidersResult.Success -> {
                when (val tokensResult = coronaCheckRepository.accessTokens(jwt)) {
                    is NetworkRequestResult.Failed -> return EventsResult.Error(tokensResult)
                    is NetworkRequestResult.Success -> tokensResult.response
                }
            }
        }
        // Fetch event providers that have events for us
        val eventProviderWithTokensResults = getEventProvidersWithTokensUseCase.get(
            eventProviders = eventProvidersResult.eventProviders,
            tokens = tokens.tokens,
            originType = originType,
            targetProviderIds = targetProviderIds
        )

        val eventProvidersWithTokensSuccessResults =
            eventProviderWithTokensResults.filterIsInstance<EventProviderWithTokenResult.Success>()
        val eventProvidersWithTokensErrorResults =
            eventProviderWithTokensResults.filterIsInstance<EventProviderWithTokenResult.Error>()

        return if (eventProvidersWithTokensSuccessResults.isNotEmpty()) {
            // We have received providers that claim to have events for us so we get those events for each provider
            val eventResults = eventProvidersWithTokensSuccessResults.map {
                getRemoteEventsUseCase.getRemoteEvents(
                    eventProvider = it.eventProvider,
                    token = it.token,
                    originType = originType
                )
            }

            // All successful responses
            val eventSuccessResults =
                eventResults.filterIsInstance<RemoteEventsResult.Success>()

            // All failed responses
            val eventFailureResults =
                eventResults.filterIsInstance<RemoteEventsResult.Error>()

            if (eventSuccessResults.isNotEmpty()) {
                // If we have success responses
                val signedModels = eventSuccessResults.map { it.signedModel }
                val hasEvents = signedModels.map { it.model }
                    .any { it.events?.isNotEmpty() ?: false }

                if (!hasEvents) {
                    // But we do not have any events
                    val missingEvents = eventProvidersWithTokensErrorResults.isNotEmpty() || eventFailureResults.isNotEmpty()
                    val errorResults: List<ErrorResult> = if (missingEvents) {
                        eventProvidersWithTokensErrorResults.map { it.errorResult } + eventFailureResults.map { it.errorResult }
                    } else {
                        emptyList()
                    }
                    EventsResult.HasNoEvents(
                        missingEvents = missingEvents,
                        errorResults = errorResults
                    )
                } else {
                    // We do have events
                    EventsResult.Success(
                        signedModels = signedModels,
                        missingEvents = eventProvidersWithTokensErrorResults.isNotEmpty() || eventFailureResults.isNotEmpty()
                    )
                }
            } else {
                // We don't have any successful responses from retrieving events for providers
                EventsResult.Error(eventFailureResults.map { it.errorResult })
            }
        } else {
            if (eventProvidersWithTokensErrorResults.isEmpty()) {
                // There are no successful responses and no error responses so no events
                EventsResult.HasNoEvents(missingEvents = false)
            } else {
                // We don't have any successful responses but do have error responses
                EventsResult.Error(eventProvidersWithTokensErrorResults.map { it.errorResult })
            }
        }

    }
}

sealed class EventsResult {
    data class Success (
        val signedModels: List<SignedResponseWithModel<RemoteProtocol3>>,
        val missingEvents: Boolean
    ) :
        EventsResult()
    data class HasNoEvents(val missingEvents: Boolean, val errorResults: List<ErrorResult> = emptyList()) : EventsResult()

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
    }
}
