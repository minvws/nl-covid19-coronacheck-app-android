package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult

/**
 * Inserts the green cards fetched from remote into the database
 */
interface SyncRemoteGreenCardsUseCase {
    suspend fun execute(remoteGreenCards: RemoteGreenCards): SyncRemoteGreenCardsResult
}

class SyncRemoteGreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val createDomesticGreenCardUseCase: CreateDomesticGreenCardUseCase,
    private val createEuGreenCardsUseCase: CreateEuGreenCardUseCase,
    private val mobileCoreWrapper: MobileCoreWrapper
    ): SyncRemoteGreenCardsUseCase {

    override suspend fun execute(remoteGreenCards: RemoteGreenCards): SyncRemoteGreenCardsResult {
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

            domesticCredentials?.let { domesticCredentials ->
                remoteGreenCards.domesticGreencard?.let { domesticGreenCard ->
                    createDomesticGreenCardUseCase.create(
                        greenCard = domesticGreenCard,
                        domesticCredentials = domesticCredentials,
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