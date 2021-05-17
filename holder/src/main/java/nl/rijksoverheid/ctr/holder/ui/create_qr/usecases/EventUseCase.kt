package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
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
    suspend fun getVaccinationEvents(digidToken: String): EventResult
}

class EventUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val eventProviderRepository: EventProviderRepository
) : EventUseCase {

    override suspend fun getVaccinationEvents(digidToken: String): EventResult {
        return try {

            // Fetch event providers
            val eventProviders = configProvidersUseCase.eventProviders()

            // Fetch access tokens
            val accessTokens = coronaCheckRepository.accessTokens(digidToken)

            // Map event providers to access tokens
            val eventProvidersWithAccessTokenMap =
                eventProviders.associateWith { eventProvider -> accessTokens.tokens.first { eventProvider.providerIdentifier == it.providerIdentifier } }

            // A list of event providers that have events
            val eventProviderWithEvents = eventProvidersWithAccessTokenMap.filter {
                val eventProvider = it.key
                val accessToken = it.value

                try {
                    val unomi = eventProviderRepository.unomi(
                        url = eventProvider.unomiUrl,
                        token = accessToken.unomi
                    )
                    unomi.informationAvailable
                } catch (e: HttpException) {
                    false
                } catch (e: IOException) {
                    false
                }
            }

            // Get vaccination events from event providers
            val remoteEvents = eventProviderWithEvents.map {
                val eventProvider = it.key
                val accessToken = it.value

                eventProviderRepository
                    .event(
                        url = eventProvider.eventUrl,
                        token = accessToken.event,
                        signingCertificateBytes = eventProvider.cms
                    )
            }

            Timber.v("VACFLOW: Fetched events: $remoteEvents")

            val vaccinationEvents = remoteEvents.map { it.model }.map { it.events }.flatten()

            EventResult.Success(
                vaccinationEvents = vaccinationEvents,
                signedModels = remoteEvents
            )
        } catch (ex: HttpException) {
            return EventResult.ServerError(ex.code())
        } catch (ex: IOException) {
            return EventResult.NetworkError
        }
    }
}

sealed class EventResult {
    data class Success(
        val vaccinationEvents: List<RemoteEvents.Event>,
        val signedModels: List<SignedResponseWithModel<RemoteEvents>>
    ) :
        EventResult()

    data class ServerError(val httpCode: Int) : EventResult()
    object NetworkError : EventResult()
}
