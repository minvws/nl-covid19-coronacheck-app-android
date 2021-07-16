package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import retrofit2.HttpException
import java.io.IOException

/**
 * Get green cards from remote
 */
interface GetRemoteGreenCardsUseCase {
    suspend fun get(events: List<EventGroupEntity>): RemoteGreenCardsResult
}

class GetRemoteGreenCardsUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val secretKeyUseCase: SecretKeyUseCase,
) : GetRemoteGreenCardsUseCase {

    override suspend fun get(events: List<EventGroupEntity>): RemoteGreenCardsResult {
        return try {
            val prepareIssue = coronaCheckRepository.getPrepareIssue()

            val commitmentMessage = mobileCoreWrapper.createCommitmentMessage(
                secretKey = secretKeyUseCase.json().toByteArray(),
                prepareIssueMessage = prepareIssue.prepareIssueMessage
            )

            val remoteGreenCards =  coronaCheckRepository.getGreenCards(
                stoken = prepareIssue.stoken,
                events = events.map { String(it.jsonData) },
                issueCommitmentMessage = commitmentMessage
            )
            RemoteGreenCardsResult.Success(remoteGreenCards)
        } catch (e: HttpException) {
            RemoteGreenCardsResult.Error.ServerError(e.code())
        } catch (e: IOException) {
            RemoteGreenCardsResult.Error.NetworkError
        } catch (e: Exception) {
            RemoteGreenCardsResult.Error.ServerError(200)
        }
    }
}

sealed class RemoteGreenCardsResult {
    data class Success (val remoteGreenCards: RemoteGreenCards): RemoteGreenCardsResult()
    sealed class Error: RemoteGreenCardsResult() {
        object NetworkError : Error()
        data class ServerError(val httpCode: Int) : Error()
    }
}