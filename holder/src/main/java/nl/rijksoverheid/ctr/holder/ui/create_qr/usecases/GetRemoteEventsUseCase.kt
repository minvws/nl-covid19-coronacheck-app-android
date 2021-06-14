package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import retrofit2.HttpException
import java.io.IOException

/**
 * Get events for a event provider
 */
interface GetRemoteEventsUseCase {
    suspend fun getVaccinations(eventProvider: RemoteConfigProviders.EventProvider,
                token: RemoteAccessTokens.Token): RemoteEventsResult<RemoteEventsVaccinations>

    suspend fun getTestResults(eventProvider: RemoteConfigProviders.EventProvider,
                                token: RemoteAccessTokens.Token): RemoteEventsResult<RemoteEventsNegativeTests>
}

class GetRemoteEventsUseCaseImpl(private val eventProviderRepository: EventProviderRepository): GetRemoteEventsUseCase {
    override suspend fun getVaccinations(
        eventProvider: RemoteConfigProviders.EventProvider,
        token: RemoteAccessTokens.Token): RemoteEventsResult<RemoteEventsVaccinations> {

        return try {
            val events = eventProviderRepository
                .vaccinationEvents(
                    url = eventProvider.eventUrl,
                    token = token.event,
                    signingCertificateBytes = eventProvider.cms
                )

            RemoteEventsResult.Success(events)
        } catch (e: HttpException) {
            RemoteEventsResult.Error.ServerError(
                httpCode = e.code()
            )
        } catch (e: IOException) {
            RemoteEventsResult.Error.NetworkError
        }
    }

    override suspend fun getTestResults(
        eventProvider: RemoteConfigProviders.EventProvider,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult<RemoteEventsNegativeTests> {

        return try {
            val events = eventProviderRepository
                .negativeTestEvent(
                    url = eventProvider.eventUrl,
                    token = token.event,
                    signingCertificateBytes = eventProvider.cms
                )

            RemoteEventsResult.Success(events)
        } catch (e: HttpException) {
            RemoteEventsResult.Error.ServerError(
                httpCode = e.code()
            )
        } catch (e: IOException) {
            RemoteEventsResult.Error.NetworkError
        }
    }
}

sealed class RemoteEventsResult<out T> {
    data class Success<T: RemoteEvent>(val signedModel: SignedResponseWithModel<T>): RemoteEventsResult<T>()
    sealed class Error: RemoteEventsResult<Nothing>() {
        data class ServerError(val httpCode: Int): Error()
        object NetworkError : Error()
    }
}
