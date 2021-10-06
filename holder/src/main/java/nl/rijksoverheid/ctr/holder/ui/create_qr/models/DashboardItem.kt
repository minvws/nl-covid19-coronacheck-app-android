package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState

sealed class DashboardItem {

    data class HeaderItem(@StringRes val text: Int) : DashboardItem()
    data class PlaceholderCardItem(val greenCardType: GreenCardType) : DashboardItem()
    object ClockDeviationItem : DashboardItem()
    object SyncGreenCardsItem : DashboardItem()
    object GreenCardsSyncedItem: DashboardItem()

    data class CardsItem(val cards: List<CardItem>) : DashboardItem() {

        sealed class CredentialState {
            data class HasCredential(val credential: CredentialEntity) : CredentialState()
            object LoadingCredential : CredentialState()
            object NoCredential : CredentialState()
        }

        data class CardItem(
            val greenCard: GreenCard,
            val originStates: List<OriginState>,
            val credentialState: CredentialState,
            val databaseSyncerResult: DatabaseSyncerResult
        )
    }

    data class GreenCardExpiredItem(val greenCard: GreenCard) : DashboardItem()
    data class OriginInfoItem(val greenCardType: GreenCardType, val originType: OriginType) : DashboardItem()
    data class AddQrButtonItem(val show: Boolean) : DashboardItem()
}