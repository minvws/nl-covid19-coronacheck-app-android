package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
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
interface EventUseCase {
    suspend fun getEvents(): EventResult
}

class EventUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val testProviderRepository: TestProviderRepository
) : EventUseCase {

    override suspend fun getEvents(): EventResult {
        return try {
            val eventProviders = configProvidersUseCase.eventProviders()
            Timber.v("VACFLOW: Fetched event providers: $eventProviders")

            val accessTokens = coronaCheckRepository.accessTokens("999999011")
            Timber.v("VACFLOW: Fetched access tokens: $accessTokens")

            val eventProvidersWithAccessTokenMap =
                eventProviders.associateWith { eventProvider -> accessTokens.tokens.first { eventProvider.providerIdentifier == it.providerIdentifier } }

            Timber.v("VACFLOW: Mapped event providers to access token: $eventProvidersWithAccessTokenMap")

            // A list of event providers that have events
            val eventProviderWithEvents = eventProvidersWithAccessTokenMap.filter {
                val eventProvider = it.key
                val accessToken = it.value

                val correctUrl = if (eventProvider.unomiUrl.contains("https")) {
                    eventProvider.unomiUrl
                } else {
                    eventProvider.unomiUrl.replace("http", "https")
                }

                try {
                    val unomi = testProviderRepository.unomi(
                        url = correctUrl,
                        token = accessToken.unomi
                    )
                    unomi.informationAvailable
                } catch (e: HttpException) {
                    false
                } catch (e: IOException) {
                    false
                }
            }.keys.toList()
            
            Timber.v("VACFLOW: Event providers with events: $eventProviderWithEvents")

            EventResult.Success(
                remoteEvents = RemoteEvents("1")
            )
        } catch (ex: HttpException) {
            return EventResult.ServerError(ex.code())
        } catch (ex: IOException) {
            return EventResult.NetworkError
        }
    }
}

sealed class EventResult {
    data class Success(val remoteEvents: RemoteEvents) : EventResult()
    data class ServerError(val httpCode: Int) : EventResult()
    object NetworkError : EventResult()
}
