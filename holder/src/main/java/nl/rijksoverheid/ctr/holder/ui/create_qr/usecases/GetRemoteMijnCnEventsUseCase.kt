package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.MijnCnEventProviderRepository
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/**
 * Get events for a event provider
 */
interface GetRemoteMijnCnEventsUseCase {
    suspend fun getRemoteEvents(
        eventProvider: RemoteConfigProviders.EventProvider,
        filter: String,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult
}

class GetRemoteMijnCnEventsUseCaseImpl(
    private val mijnCnEventProviderRepository: MijnCnEventProviderRepository
) : GetRemoteMijnCnEventsUseCase {

    override suspend fun getRemoteEvents(
        eventProvider: RemoteConfigProviders.EventProvider,
        filter: String,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult {

        return when (val eventsResult = mijnCnEventProviderRepository
            .getMijnCnEvents(
                url = eventProvider.eventUrl,
                token = token.event,
                filter = filter,
                signingCertificateBytes = eventProvider.cms,
                provider = eventProvider.providerIdentifier,
            )) {

            is NetworkRequestResult.Success<SignedResponseWithModel<RemoteProtocol3>> ->
                RemoteEventsResult.Success(eventsResult.response)
            is NetworkRequestResult.Failed -> RemoteEventsResult.Error(eventsResult)
        }
    }
}
