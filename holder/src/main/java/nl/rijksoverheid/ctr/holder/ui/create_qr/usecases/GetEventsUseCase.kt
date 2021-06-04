package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import retrofit2.HttpException
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetEventsUseCase {
    suspend fun getVaccinationEvents(jwt: String): EventsResult<RemoteEventsVaccinations>
    suspend fun getTestResult3Events(jwt: String): EventsResult<RemoteEventsNegativeTests>
}

class GetEventsUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val eventProviderRepository: EventProviderRepository
) : GetEventsUseCase {

    sealed class EventProviderWithEventResult{
        data class Success(val eventProviderWithEvents: Map<RemoteConfigProviders.EventProvider, RemoteAccessTokens.Token>):
            EventProviderWithEventResult()

        object TooBusy: EventProviderWithEventResult()
    }

    private suspend fun getEventProviderWithEvents(jwt: String, originType: OriginType): EventProviderWithEventResult {
        // Fetch event providers
        val eventProviders = configProvidersUseCase.eventProviders()

        // Fetch access tokens
        val accessTokens = coronaCheckRepository.accessTokens(jwt)

        // Map event providers to access tokens
        val eventProvidersWithAccessTokenMap =
            eventProviders.associateWith { eventProvider -> accessTokens.tokens.first { eventProvider.providerIdentifier == it.providerIdentifier } }

        // A list of event providers that have events
        val eventProviderWithEvents = eventProvidersWithAccessTokenMap.filter {
            val eventProvider = it.key
            val accessToken = it.value

            when (originType) {
                is OriginType.Test -> {
                    try {
                        val unomi = eventProviderRepository.unomiTestEvents(
                            url = eventProvider.unomiUrl,
                            token = accessToken.unomi
                        )
                        unomi.informationAvailable
                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            return EventProviderWithEventResult.TooBusy
                        }
                        false
                    } catch (e: IOException) {
                        false
                    }
                }
                is OriginType.Vaccination -> {
                    try {
                        val unomi = eventProviderRepository.unomiVaccinationEvents(
                            url = eventProvider.unomiUrl,
                            token = accessToken.unomi
                        )
                        unomi.informationAvailable
                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            return EventProviderWithEventResult.TooBusy
                        }
                        false
                    } catch (e: IOException) {
                        false
                    }
                }
                is OriginType.Recovery -> {
                    // TODO
                    false
                }
            }
        }

        return EventProviderWithEventResult.Success(eventProviderWithEvents)
    }

    override suspend fun getVaccinationEvents(jwt: String): EventsResult<RemoteEventsVaccinations>{
        return when (val result = getEventProviderWithEvents(jwt, OriginType.Vaccination)) {
            is EventProviderWithEventResult.Success -> {
                try {
                    val vaccinationEvents = result.eventProviderWithEvents.map {
                        val eventProvider = it.key
                        val accessToken = it.value

                        eventProviderRepository
                            .vaccinationEvents(
                                url = eventProvider.eventUrl,
                                token = accessToken.event,
                                signingCertificateBytes = eventProvider.cms
                            )
                    }

                    EventsResult.Success(
                        signedModels = vaccinationEvents
                    )
                } catch (e: HttpException) {
                    return if (e.code() == 429) {
                        EventsResult.TooBusy
                    } else {
                        EventsResult.ServerError(e.code())
                    }
                } catch (e: IOException) {
                    return EventsResult.NetworkError
                }
            }
            is EventProviderWithEventResult.TooBusy -> {
                return EventsResult.TooBusy
            }
        }
    }

    override suspend fun getTestResult3Events(jwt: String): EventsResult<RemoteEventsNegativeTests> {
        return when (val result = getEventProviderWithEvents(jwt, OriginType.Test)) {
            is EventProviderWithEventResult.Success -> {
                try {
                    val negativeTestEvents = result.eventProviderWithEvents.map {
                        val eventProvider = it.key
                        val accessToken = it.value

                        eventProviderRepository
                            .negativeTestEvent(
                                url = eventProvider.eventUrl,
                                token = accessToken.event,
                                signingCertificateBytes = eventProvider.cms
                            )
                    }

                    EventsResult.Success(
                        signedModels = negativeTestEvents
                    )
                } catch (e: HttpException) {
                    return if (e.code() == 429) {
                        EventsResult.TooBusy
                    } else {
                        EventsResult.ServerError(e.code())
                    }
                } catch (e: IOException) {
                    return EventsResult.NetworkError
                }
            }
            is EventProviderWithEventResult.TooBusy -> {
                return EventsResult.TooBusy
            }
        }
    }
}

sealed class EventsResult<out T> {
    data class Success<T> (
        val signedModels: List<SignedResponseWithModel<T>>
    ) :
        EventsResult<T>()
    data class ServerError(val httpCode: Int) : EventsResult<Nothing>()
    object TooBusy : EventsResult<Nothing>()
    object NetworkError : EventsResult<Nothing>()
}
