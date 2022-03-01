package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.ProtocolOrigin
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventProvider
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.ScopeUtil
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
interface GetDigidEventsUseCase {
    suspend fun getEvents(
        jwt: String,
        originTypes: List<RemoteOriginType>
    ): EventsResult
}

class GetDigidEventsUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase,
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase,
    private val scopeUtil: ScopeUtil
) : GetDigidEventsUseCase {

    override suspend fun getEvents(
        jwt: String,
        originTypes: List<RemoteOriginType>
    ): EventsResult {
        // Fetch event providers
        val eventProvidersResult = configProvidersUseCase.eventProviders()
        val (tokens, remoteEventProviders) = when (eventProvidersResult) {
            is EventProvidersResult.Error -> return EventsResult.Error(eventProvidersResult.errorResult)
            is EventProvidersResult.Success -> {
                when (val tokensResult = coronaCheckRepository.accessTokens(jwt)) {
                    is NetworkRequestResult.Failed -> return EventsResult.Error(tokensResult)
                    is NetworkRequestResult.Success -> Pair(
                        tokensResult.response,
                        eventProvidersResult.eventProviders
                    )
                }
            }
        }
        val eventProviders = eventProvidersResult.eventProviders

        val eventProviderWithTokensResults =
            mutableMapOf<RemoteOriginType, List<EventProviderWithTokenResult>>()
        originTypes.forEach { originType ->
            val targetProviderIds = eventProviders.filter {
                it.supports(originType)
            }.map { it.providerIdentifier.lowercase() }

            if (targetProviderIds.isEmpty()) {
                return EventsResult.Error.noProvidersError(originType)
            }

            val filter = EventProviderRepository.getFilter(originType)

            val scope = scopeUtil.getScopeForRemoteOriginType(
                remoteOriginType = originType,
                getPositiveTestWithVaccination = originTypes.size > 1
            )

            // Fetch event providers that have events for us
            eventProviderWithTokensResults[originType] = getEventProvidersWithTokensUseCase.get(
                eventProviders = eventProviders,
                tokens = tokens.tokens,
                filter = filter,
                scope = scope,
                targetProviderIds = targetProviderIds
            )
        }

        val eventProvidersWithTokensSuccessResults =
            eventProviderWithTokensResults.mapValues {
                it.value.filterIsInstance<EventProviderWithTokenResult.Success>()
            }
        val eventProvidersWithTokensErrorResults =
            eventProviderWithTokensResults.values.flatten()
                .filterIsInstance<EventProviderWithTokenResult.Error>()

        return if (eventProvidersWithTokensSuccessResults.flatMap { it.value }.isNotEmpty()) {
            val eventResults = mutableMapOf<RemoteOriginType, List<RemoteEventsResult>>()
            eventProvidersWithTokensSuccessResults.forEach { (originType, eventProviders) ->
                // We have received providers that claim to have events for us so we get those events for each provider
                val events = eventProviders.map {
                    getRemoteEventsUseCase.getRemoteEvents(
                        eventProvider = it.eventProvider,
                        token = it.token,
                        filter = EventProviderRepository.getFilter(originType),
                        scope = scopeUtil.getScopeForRemoteOriginType(
                            remoteOriginType = originType,
                            getPositiveTestWithVaccination = originTypes.size > 1
                        )
                    )
                }
                eventResults[originType] = events
            }

            // All successful responses
            val eventSuccessResults =
                eventResults.mapValues {
                    it.value.filterIsInstance<RemoteEventsResult.Success>()
                }
            // All failed responses
            val eventFailureResults =
                eventResults.values.flatten().filterIsInstance<RemoteEventsResult.Error>()

            if (eventSuccessResults.flatMap { it.value }.isNotEmpty()) {
                // If we have success responses
                val signedModels = eventSuccessResults
                    .mapValues { events ->
                        events.value
                            .map { it.signedModel }
                            .filter { it.model.hasEvents() }
                    }
                    .filterValues { it.isNotEmpty() }

                if (signedModels.isEmpty()) {
                    // But we do not have any events
                    val missingEvents =
                        eventProvidersWithTokensErrorResults.isNotEmpty() || eventFailureResults.isNotEmpty()
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
                        protocolOrigins = signedModels.mapValues {
                            it.value.associate { signedModel ->
                                signedModel.model to signedModel.rawResponse
                            }
                        }.map { ProtocolOrigin(it.key.toOriginType(), it.value) },
                        missingEvents = eventProvidersWithTokensErrorResults.isNotEmpty() || eventFailureResults.isNotEmpty(),
                        eventProviders = remoteEventProviders.map {
                            EventProvider(
                                it.providerIdentifier,
                                it.name
                            )
                        }
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

