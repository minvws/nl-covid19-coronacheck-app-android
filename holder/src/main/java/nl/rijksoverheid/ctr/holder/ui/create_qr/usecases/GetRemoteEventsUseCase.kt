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
                token: RemoteAccessTokens.Token): RemoteEventsResult

    suspend fun getTestResults(eventProvider: RemoteConfigProviders.EventProvider,
                                token: RemoteAccessTokens.Token): RemoteEventsResult

    suspend fun getPositiveAndRecoveryTestResults(eventProvider: RemoteConfigProviders.EventProvider,
                               token: RemoteAccessTokens.Token): RemoteEventsResult
}

class GetRemoteEventsUseCaseImpl(private val eventProviderRepository: EventProviderRepository): GetRemoteEventsUseCase {
    override suspend fun getVaccinations(
        eventProvider: RemoteConfigProviders.EventProvider,
        token: RemoteAccessTokens.Token): RemoteEventsResult {

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
        } catch (e: Exception) {
            // In case the event provider gives us back a 200 with json we are not expecting
            RemoteEventsResult.Error.ServerError(
                httpCode = 200
            )
        }
    }

    override suspend fun getTestResults(
        eventProvider: RemoteConfigProviders.EventProvider,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult {

        return try {
            val events = eventProviderRepository
                .negativeTestEvents(
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
        } catch (e: Exception) {
            // In case the event provider gives us back a 200 with json we are not expecting
            RemoteEventsResult.Error.ServerError(
                httpCode = 200
            )
        }
    }

    override suspend fun getPositiveAndRecoveryTestResults(
        eventProvider: RemoteConfigProviders.EventProvider,
        token: RemoteAccessTokens.Token
    ): RemoteEventsResult {

        return try {
            val events = eventProviderRepository
                .positiveAndRecoveryTestEvents(
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
        } catch (e: Exception) {
            // In case the event provider gives us back a 200 with json we are not expecting
            RemoteEventsResult.Error.ServerError(
                httpCode = 200
            )
        }
    }
}

sealed class RemoteEventsResult {
    data class Success(val signedModel: SignedResponseWithModel<RemoteProtocol3>): RemoteEventsResult()
    sealed class Error: RemoteEventsResult() {
        data class ServerError(val httpCode: Int): Error()
        object NetworkError : Error()
    }
}
