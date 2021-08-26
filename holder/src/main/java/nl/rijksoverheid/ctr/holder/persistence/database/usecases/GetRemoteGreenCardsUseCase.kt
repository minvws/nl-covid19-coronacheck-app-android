package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
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

            val remoteGreenCardsResult =  coronaCheckRepository.getGreenCards(
                stoken = prepareIssue.stoken,
                events = events.map { String(it.jsonData) },
                issueCommitmentMessage = commitmentMessage
            )

            when (remoteGreenCardsResult) {
                is NetworkRequestResult.Success -> {
                    RemoteGreenCardsResult.Success(remoteGreenCardsResult.response)
                }
                is NetworkRequestResult.Failed -> {
                    RemoteGreenCardsResult.Error(remoteGreenCardsResult)
                }
            }
        } catch (e: Exception) {
            RemoteGreenCardsResult.Error(AppErrorResult(HolderStep.GetCredentialsNetworkRequest, e))
        }
    }
}

sealed class RemoteGreenCardsResult {
    data class Success(val remoteGreenCards: RemoteGreenCards): RemoteGreenCardsResult()
    data class Error(val errorResult: ErrorResult): RemoteGreenCardsResult()
}