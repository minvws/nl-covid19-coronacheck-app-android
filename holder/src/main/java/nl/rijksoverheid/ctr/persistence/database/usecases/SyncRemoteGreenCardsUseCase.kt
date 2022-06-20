package nl.rijksoverheid.ctr.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult

/**
 * Inserts the green cards fetched from remote into the database
 */
interface SyncRemoteGreenCardsUseCase {
    suspend fun execute(remoteGreenCards: RemoteGreenCards, secretKey: String): SyncRemoteGreenCardsResult
}

class SyncRemoteGreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val createDomesticGreenCardUseCase: CreateDomesticGreenCardUseCase,
    private val createEuGreenCardsUseCase: CreateEuGreenCardUseCase,
    private val mobileCoreWrapper: MobileCoreWrapper
    ): SyncRemoteGreenCardsUseCase {

    override suspend fun execute(remoteGreenCards: RemoteGreenCards, secretKey: String): SyncRemoteGreenCardsResult {
        try {
            // Create credentials
            val domesticCredentials = if (remoteGreenCards.domesticGreencard != null) {
                mobileCoreWrapper.createDomesticCredentials(
                    createCredentials = remoteGreenCards.domesticGreencard.createCredentialMessages
                )
            } else null

            // Clear everything from the database
            holderDatabase.greenCardDao().deleteAll()
            holderDatabase.originDao().deleteAll()
            holderDatabase.credentialDao().deleteAll()

            domesticCredentials?.let { it ->
                remoteGreenCards.domesticGreencard?.let { domesticGreenCard ->
                    createDomesticGreenCardUseCase.create(
                        greenCard = domesticGreenCard,
                        domesticCredentials = it,
                        secretKey = secretKey
                    )
                }
            }

            remoteGreenCards.euGreencards?.let {
                it.forEach { greenCard ->
                    createEuGreenCardsUseCase.create(
                        greenCard = greenCard
                    )
                }
            }
            return SyncRemoteGreenCardsResult.Success
        } catch (e: Exception) {
            return SyncRemoteGreenCardsResult.Failed(
                AppErrorResult(
                    step = HolderStep.StoringCredentials,
                    e = e
                )
            )
        }
    }
}

sealed class SyncRemoteGreenCardsResult {
    object Success: SyncRemoteGreenCardsResult()
    data class Failed(val errorResult: ErrorResult): SyncRemoteGreenCardsResult()
}