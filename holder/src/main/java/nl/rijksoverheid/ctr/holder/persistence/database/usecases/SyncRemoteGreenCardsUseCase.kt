package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards

/**
 * Inserts the green cards fetched from remote into the database
 */
interface SyncRemoteGreenCardsUseCase {
    suspend fun execute(remoteGreenCards: RemoteGreenCards)
}

class SyncRemoteGreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val createDomesticGreenCardUseCase: CreateDomesticGreenCardUseCase,
    private val createEuGreenCardsUseCase: CreateEuGreenCardsUseCase
    ): SyncRemoteGreenCardsUseCase {

    override suspend fun execute(remoteGreenCards: RemoteGreenCards) {

        // Clear everything from the database
        holderDatabase.greenCardDao().deleteAll()
        holderDatabase.originDao().deleteAll()
        holderDatabase.credentialDao().deleteAll()

        remoteGreenCards.domesticGreencard?.let {
            createDomesticGreenCardUseCase.create(
                greenCard = it
            )
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