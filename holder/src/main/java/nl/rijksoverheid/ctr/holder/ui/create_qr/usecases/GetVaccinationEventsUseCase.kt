package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import retrofit2.HttpException
import java.io.IOException


interface GetVaccinationEventsUseCase {
    suspend fun get(eventProvider: RemoteConfigProviders.EventProvider, token: RemoteAccessTokens.Token): GetVaccinationEventsResult
}

class GetVaccinationEventsUseCaseImpl(private val eventProviderRepository: EventProviderRepository): GetVaccinationEventsUseCase {

    override suspend fun get(eventProvider: RemoteConfigProviders.EventProvider, token: RemoteAccessTokens.Token): GetVaccinationEventsResult {
        return try {
            val events = eventProviderRepository
                .vaccinationEvents(
                    url = eventProvider.eventUrl,
                    token = token.event,
                    signingCertificateBytes = eventProvider.cms
                )
            GetVaccinationEventsResult.Success(
                signedModel = events
            )
        } catch (e: HttpException) {
            GetVaccinationEventsResult.Error.ServerError(
                httpCode = e.code()
            )
        } catch (e: IOException) {
            GetVaccinationEventsResult.Error.NetworkError
        }
    }
}

sealed class GetVaccinationEventsResult {
    data class Success(val signedModel: SignedResponseWithModel<RemoteEventsVaccinations>):
        GetVaccinationEventsResult()
    sealed class Error: GetVaccinationEventsResult() {
        data class ServerError(val httpCode: Int): Error()
        object NetworkError: Error()
    }
}
