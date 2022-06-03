/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.api.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/**
 * Get events for a event provider
 */
interface GetRemoteEventsUseCase {
    suspend fun getRemoteEvents(
        eventProvider: RemoteConfigProviders.EventProvider,
        filter: String,
        scope: String?,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult
}

class GetRemoteEventsUseCaseImpl(private val eventProviderRepository: EventProviderRepository) :
    GetRemoteEventsUseCase {

    override suspend fun getRemoteEvents(
        eventProvider: RemoteConfigProviders.EventProvider,
        filter: String,
        scope: String?,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult {

        return when (val eventsResult = eventProviderRepository
            .getEvents(
                url = eventProvider.eventUrl,
                token = token.event,
                filter = filter,
                scope = scope,
                signingCertificateBytes = eventProvider.cms,
                provider = eventProvider.providerIdentifier,
                tlsCertificateBytes = eventProvider.tls,
            )) {

            is NetworkRequestResult.Success<SignedResponseWithModel<RemoteProtocol>> ->
                RemoteEventsResult.Success(eventsResult.response)
            is NetworkRequestResult.Failed -> RemoteEventsResult.Error(eventsResult)
        }
    }
}

sealed class RemoteEventsResult {
    data class Success(val signedModel: SignedResponseWithModel<RemoteProtocol>) :
        RemoteEventsResult()

    data class Error(val errorResult: ErrorResult) : RemoteEventsResult()
}
