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

    sealed class InfoItem(
        val isDismissible: Boolean,
        val hasReadMore: Boolean
    ) : DashboardItem() {

        object RefreshEuVaccinations :
            InfoItem(isDismissible = false, hasReadMore = true)

        object ExtendDomesticRecovery :
            InfoItem(isDismissible = false, hasReadMore = true)

        object RecoverDomesticRecovery :
            InfoItem(isDismissible = false, hasReadMore = true)

        data class ConfigFreshnessWarning(val maxValidityDate: Long) :
            InfoItem(isDismissible = false, hasReadMore = true)

        object RefreshedEuVaccinations : InfoItem(isDismissible = true, hasReadMore = true)

        object ExtendedDomesticRecovery : InfoItem(isDismissible = true, hasReadMore = true)

        object RecoveredDomesticRecovery : InfoItem(isDismissible = true, hasReadMore = true)

        data class OriginInfoItem(
            val greenCardType: GreenCardType,
            val originType: OriginType,
        ) : InfoItem(isDismissible = false, hasReadMore = true)

        object ClockDeviationItem : InfoItem(isDismissible = false, hasReadMore = true)

        data class GreenCardExpiredItem(val greenCard: GreenCard) :
            InfoItem(isDismissible = true, hasReadMore = false)
    }

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

    data class AddQrButtonItem(val show: Boolean) : DashboardItem()
}