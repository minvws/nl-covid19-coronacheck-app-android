package nl.rijksoverheid.ctr.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.json.JSONObject

/**
 * Get green cards from remote
 */
interface GetRemoteGreenCardsUseCase {
    suspend fun get(
        events: List<EventGroupEntity>,
        secretKey: String,
        flow: Flow
    ): RemoteGreenCardsResult
}

class GetRemoteGreenCardsUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase
) : GetRemoteGreenCardsUseCase {

    override suspend fun get(
        events: List<EventGroupEntity>,
        secretKey: String,
        flow: Flow
    ): RemoteGreenCardsResult {
        return try {
            val prepareIssue =
                when (val prepareIssueResult = coronaCheckRepository.getPrepareIssue()) {
                    is NetworkRequestResult.Success -> {
                        prepareIssueResult.response
                    }
                    is NetworkRequestResult.Failed -> {
                        return RemoteGreenCardsResult.Error(prepareIssueResult)
                    }
                }

            val commitmentMessage = try {
                mobileCoreWrapper.createCommitmentMessage(
                    secretKey = secretKey.toByteArray(),
                    prepareIssueMessage = prepareIssue.prepareIssueMessage
                )
            } catch (e: Exception) {
                return RemoteGreenCardsResult.Error(
                    AppErrorResult(
                        step = HolderStep.PrepareIssueNetworkRequest,
                        e = e
                    )
                )
            }

            val remoteGreenCardsResult = coronaCheckRepository.getGreenCards(
                stoken = prepareIssue.stoken,
                events = events.map {
                    val jsonObject = JSONObject(it.jsonData.decodeToString())
                    jsonObject.put("id", it.id.toString())
                    jsonObject.toString()
                },
                issueCommitmentMessage = commitmentMessage,
                flow = flow
            )

            when (remoteGreenCardsResult) {
                is NetworkRequestResult.Success -> {
                    val blockedEventIds =
                        remoteGreenCardsResult.response.blobExpireDates?.filter { it.reason == "event_blocked" }
                            ?: listOf()
                    val blockedEvents = blockedEventIds.mapNotNull { blobExpiry ->
                        val eventGroup = events.firstOrNull { event -> event.id == blobExpiry.id }
                        val remoteProtocol =
                            eventGroup?.let { getRemoteProtocolFromEventGroupUseCase.get(it) }
                        remoteProtocol?.events?.mapNotNull { remoteEvent ->
                            BlockedEvent(
                                eventGroup.id,
                                remoteEvent
                            )
                        }
                    }.flatten()
                    RemoteGreenCardsResult.Success(remoteGreenCardsResult.response, blockedEvents)
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
    data class Success(
        val remoteGreenCards: RemoteGreenCards,
        val blockedEvents: List<BlockedEvent> = listOf()
    ) : RemoteGreenCardsResult()

    data class Error(val errorResult: ErrorResult) : RemoteGreenCardsResult()
}

data class BlockedEvent(
    val eventGroupId: Int,
    val remoteEvent: RemoteEvent
)
