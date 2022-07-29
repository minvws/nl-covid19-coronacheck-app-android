/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.usecases

import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.api.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.EventsResult
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtil
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
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
    suspend fun getEvents(
        jwt: String,
        originTypes: List<RemoteOriginType>,
        loginType: LoginType
    ): EventsResult
}

class GetEventsUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase,
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase,
    private val scopeUtil: ScopeUtil
) : GetEventsUseCase {

    override suspend fun getEvents(
        jwt: String,
        originTypes: List<RemoteOriginType>,
        loginType: LoginType
    ): EventsResult {
        // Fetch event providers
        val eventProvidersResult = configProvidersUseCase.eventProviders()
        val (tokens, remoteEventProviders) = when (eventProvidersResult) {
            is EventProvidersResult.Error -> return EventsResult.Error(eventProvidersResult.errorResult)
            is EventProvidersResult.Success -> {
                when (loginType) {
                    is LoginType.Pap -> {
                        val fakeRemoteAccessTokens = RemoteAccessTokens(
                            tokens = eventProvidersResult.eventProviders.map {
                                RemoteAccessTokens.Token(
                                    providerIdentifier = it.providerIdentifier,
                                    unomi = jwt,
                                    event = jwt
                                )
                            }
                        )
                        Pair(
                            fakeRemoteAccessTokens,
                            eventProvidersResult.eventProviders
                        )
                    }
                    is LoginType.Max -> {
                        when (val tokensResult = coronaCheckRepository.accessTokens(jwt)) {
                            is NetworkRequestResult.Failed -> return EventsResult.Error(tokensResult)
                            is NetworkRequestResult.Success -> Pair(
                                tokensResult.response,
                                eventProvidersResult.eventProviders
                            )
                        }
                    }
                }
            }
        }
        val eventProviders = eventProvidersResult.eventProviders

        val eventProviderWithTokensResults =
            mutableMapOf<RemoteOriginType, List<EventProviderWithTokenResult>>()
        originTypes.forEach { originType ->
            val targetProviderIds = eventProviders.filter {
                it.supports(originType, loginType)
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
            }.filterValues { it.isNotEmpty() }
        val eventProvidersWithTokensErrorResults =
            eventProviderWithTokensResults.values.flatten()
                .filterIsInstance<EventProviderWithTokenResult.Error>()

        return if (eventProvidersWithTokensSuccessResults.isNotEmpty()) {
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
                        remoteEvents = signedModels.map {
                            it.value.associate { signedModel ->
                                signedModel.model to signedModel.rawResponse
                            }
                        }.fold(mapOf()) { protocol, byteArray -> protocol + byteArray },
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
