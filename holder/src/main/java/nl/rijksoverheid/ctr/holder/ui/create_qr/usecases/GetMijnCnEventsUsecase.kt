package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventUtil
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
interface GetMijnCnEventsUsecase {
    suspend fun getEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean
    ): EventsResult
}

class GetMijnCnEventsUsecaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase,
    private val remoteEventUtil: RemoteEventUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : GetMijnCnEventsUsecase {

    override suspend fun getEvents(
        jwt: String,
        originType: RemoteOriginType,
        withIncompleteVaccination: Boolean
    ): EventsResult {
        // Fetch event providers
        val eventProvidersResult = configProvidersUseCase.eventProvidersBES()
        val (remoteEventProviders) = when (eventProvidersResult) {
            is EventProvidersResult.Error -> return EventsResult.Error(eventProvidersResult.errorResult)
            is EventProvidersResult.Success -> {
                eventProvidersResult.eventProviders
            }
        }

        val eventProviders = eventProvidersResult.eventProviders
        val targetProviderIds = eventProviders.filter {
            it.supports(originType)
        }.map { it.providerIdentifier.lowercase() }

        if (targetProviderIds.isEmpty()) {
            return EventsResult.Error.noProvidersError(originType)
        }


        return if (eventProviders.isNotEmpty()) {
            // We have received providers that claim to have events for us so we get those events for each provider
            val filter = EventProviderRepository.getFilter(originType)

                val eventResults = eventProviders.map { eventProvider ->
                getRemoteEventsUseCase.getRemoteEvents(
                    eventProvider = eventProvider,
                    token = RemoteAccessTokens.Token(
                        providerIdentifier = eventProvider.providerIdentifier,
                        unomi = "",
                        event = jwt
                    ),
                    filter = filter
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
                val allEvents = signedModels.map { it.model }.mapNotNull { it.events }.flatten()
                val hasEvents = allEvents.isNotEmpty()

                if (!hasEvents) {
                    // But we do not have any events
                    val missingEvents = eventFailureResults.isNotEmpty()
                    val errorResults: List<ErrorResult> = if (missingEvents) {
                        eventFailureResults.map { it.errorResult }
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
                        missingEvents = eventFailureResults.isNotEmpty(),
                        eventProviders = eventProviders.map {
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
            if (eventProviders.isEmpty()) {
                // There are no successful responses and no error responses so no events
                EventsResult.HasNoEvents(missingEvents = false)
            } else {
                // We don't have any successful responses but do have error responses
                EventsResult.Error((eventProvidersResult as EventProvidersResult.Error).errorResult)
            }
        }

    }
}

