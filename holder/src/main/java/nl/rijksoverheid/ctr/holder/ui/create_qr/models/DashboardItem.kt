package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
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
        val hasButton: Boolean,
        @StringRes val buttonText: Int? = null
    ) : DashboardItem() {

        object RefreshEuVaccinations :
            InfoItem(isDismissible = false, hasButton = true)

        object ExtendDomesticRecovery :
            InfoItem(isDismissible = false, hasButton = true)

        object RecoverDomesticRecovery :
            InfoItem(isDismissible = false, hasButton = true)

        data class ConfigFreshnessWarning(val maxValidityDate: Long) :
            InfoItem(isDismissible = false, hasButton = true)

        object RefreshedEuVaccinations : InfoItem(isDismissible = true, hasButton = true)

        object ExtendedDomesticRecovery : InfoItem(isDismissible = true, hasButton = true)

        object RecoveredDomesticRecovery : InfoItem(isDismissible = true, hasButton = true)

        data class OriginInfoItem(
            val greenCardType: GreenCardType,
            val originType: OriginType,
        ) : InfoItem(isDismissible = false, hasButton = true)

        object MissingDutchVaccinationItem : InfoItem(isDismissible = false, hasButton = true)

        object ClockDeviationItem : InfoItem(isDismissible = false, hasButton = true)

        data class GreenCardExpiredItem(val greenCard: GreenCard) :
            InfoItem(isDismissible = true, hasButton = false)

        object AppUpdate : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.recommended_update_card_action
        )
    }

    object CoronaMelderItem : DashboardItem()

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