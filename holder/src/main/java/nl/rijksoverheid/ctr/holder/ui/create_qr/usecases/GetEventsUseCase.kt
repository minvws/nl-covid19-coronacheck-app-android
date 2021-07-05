package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

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
        return try {
            // Fetch event providers
            val eventProviders = configProvidersUseCase.eventProviders()

            // Fetch access tokens
            val tokens = coronaCheckRepository.accessTokens(jwt)

            // Fetch event providers that have events for us
            val eventProviderWithTokensResults = getEventProvidersWithTokensUseCase.get(
                eventProviders = eventProviders,
                tokens = tokens.tokens,
                originType = originType,
                targetProviderIds = targetProviderIds)

            val eventProvidersWithTokensSuccessResults = eventProviderWithTokensResults.filterIsInstance<EventProviderWithTokenResult.Success>()
            val eventProvidersWithTokensErrorResults = eventProviderWithTokensResults.filterIsInstance<EventProviderWithTokenResult.Error>()

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

                return if (eventSuccessResults.isNotEmpty()) {
                    // If we have success responses
                    val signedModels = eventSuccessResults.map { it.signedModel }
                    val hasEvents = signedModels.map { it.model }.any { it.events?.isNotEmpty() ?: false }

                    if (!hasEvents) {
                        // But we do not have any events
                        EventsResult.HasNoEvents(
                            missingEvents = eventProvidersWithTokensErrorResults.isNotEmpty() || eventFailureResults.isNotEmpty()
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
                    val isNetworkError = eventFailureResults.any { it is RemoteEventsResult.Error.NetworkError }
                    if (isNetworkError) {
                        EventsResult.Error.NetworkError
                    } else {
                        EventsResult.Error.EventProviderError.ServerError
                    }
                }
            } else {
                if (eventProvidersWithTokensErrorResults.isEmpty()) {
                    // There are no successful responses and no error responses so no events
                    return EventsResult.HasNoEvents(missingEvents = false)
                } else {
                    // We don't have any successful responses but do have error responses
                    val isNetworkError = eventProvidersWithTokensErrorResults.any { it is EventProviderWithTokenResult.Error.NetworkError }
                    if (isNetworkError) {
                        EventsResult.Error.NetworkError
                    } else {
                        EventsResult.Error.EventProviderError.ServerError
                    }
                }
            }
        } catch (e: HttpException) {
            EventsResult.Error.CoronaCheckError.ServerError(e.code())
        } catch (e: IOException) {
            EventsResult.Error.NetworkError
        } catch (e: Exception) {
            EventsResult.Error.CoronaCheckError.ServerError(200)
        }
    }
}

sealed class EventsResult {
    data class Success (
        val signedModels: List<SignedResponseWithModel<RemoteProtocol3>>,
        val missingEvents: Boolean
    ) :
        EventsResult()
    data class HasNoEvents(val missingEvents: Boolean) : EventsResult()

    sealed class Error: EventsResult() {
        object NetworkError : Error()

        sealed class CoronaCheckError: Error() {
            data class ServerError(val httpCode: Int) : CoronaCheckError()
        }

        sealed class EventProviderError: Error() {
            object ServerError : EventProviderError()
        }
    }
}
