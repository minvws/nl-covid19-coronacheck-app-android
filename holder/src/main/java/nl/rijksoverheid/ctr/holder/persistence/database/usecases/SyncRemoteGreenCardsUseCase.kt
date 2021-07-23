package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/**
 * Inserts the green cards fetched from remote into the database
 */
interface SyncRemoteGreenCardsUseCase {
    suspend fun execute(remoteGreenCards: RemoteGreenCards)
}

class SyncRemoteGreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val createDomesticGreenCardUseCase: CreateDomesticGreenCardUseCase,
    private val createEuGreenCardsUseCase: CreateEuGreenCardUseCase,
    private val mobileCoreWrapper: MobileCoreWrapper
    ): SyncRemoteGreenCardsUseCase {

    override suspend fun execute(remoteGreenCards: RemoteGreenCards) {

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
    }
}