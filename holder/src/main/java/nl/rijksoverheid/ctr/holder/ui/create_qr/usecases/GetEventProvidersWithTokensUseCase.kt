package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import android.annotation.SuppressLint
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteUnomi
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.shared.ext.filterNotNullValues
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
 * Get all event providers that have events for the [OriginType]
 */
interface GetEventProvidersWithTokensUseCase {

    /**
     * @param eventProviders A list of all the event providers
     * @param tokens A list of all tokens
     * @param originType The type of events you want to fetch
     */
    suspend fun get(
        eventProviders: List<RemoteConfigProviders.EventProvider>,
        tokens: List<RemoteAccessTokens.Token>,
        originType: OriginType,
        targetProviderIds: List<String>? = null): List<EventProviderWithTokenResult>
}

class GetEventProvidersWithTokensUseCaseImpl(
    private val eventProviderRepository: EventProviderRepository
): GetEventProvidersWithTokensUseCase {

    @SuppressLint("DefaultLocale")
    override suspend fun get(
        eventProviders: List<RemoteConfigProviders.EventProvider>,
        tokens: List<RemoteAccessTokens.Token>,
        originType: OriginType,
        targetProviderIds: List<String>?
    ): List<EventProviderWithTokenResult> {

        // Map event providers to tokens
        val allEventProvidersWithTokens =
            eventProviders
                .associateWith { eventProvider ->
                    tokens
                        .firstOrNull { eventProvider.providerIdentifier == it.providerIdentifier }
                }
                .filterNotNullValues()

        // If we want to only target specific providers ids we filter others out
        val targetEventProvidersWithTokens = allEventProvidersWithTokens.filter {
            targetProviderIds?.contains(it.key.providerIdentifier.lowercase()) ?: true
        }

        // Return a list of event providers that have events
        return targetEventProvidersWithTokens.map {
            val eventProvider = it.key
            val token = it.value

            val unomiResult = eventProviderRepository.getUnomi(
                url = eventProvider.unomiUrl,
                token = token.unomi,
                filter = EventProviderRepository.getFilter(originType),
                signingCertificateBytes = eventProvider.cms,
                provider = eventProvider.providerIdentifier,
            )

            when (unomiResult) {
                is NetworkRequestResult.Success<RemoteUnomi> -> {
                    if (unomiResult.response.informationAvailable) {
                        EventProviderWithTokenResult.Success(
                            eventProvider = eventProvider,
                            token = token
                        )
                    } else {
                        null
                    }
                }
                is NetworkRequestResult.Failed -> EventProviderWithTokenResult.Error(unomiResult)
            }


        }.filterNotNull()
    }
}

sealed class EventProviderWithTokenResult {
    data class Success(
        val eventProvider: RemoteConfigProviders.EventProvider,
        val token: RemoteAccessTokens.Token
    ) : EventProviderWithTokenResult()

    data class Error(val errorResult: ErrorResult): EventProviderWithTokenResult()
}