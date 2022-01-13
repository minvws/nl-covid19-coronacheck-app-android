package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
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

        object ExtendDomesticRecovery :
            InfoItem(isDismissible = false, hasButton = true)

        object RecoverDomesticRecovery :
            InfoItem(isDismissible = false, hasButton = true)

        data class ConfigFreshnessWarning(val maxValidityDate: Long) :
            InfoItem(isDismissible = false, hasButton = true)

        object ExtendedDomesticRecovery : InfoItem(isDismissible = true, hasButton = true)

        object RecoveredDomesticRecovery : InfoItem(isDismissible = true, hasButton = true)

        data class OriginInfoItem(
            val greenCardType: GreenCardType,
            val originType: OriginType,
        ) : InfoItem(isDismissible = false, hasButton = true)

        object MissingDutchVaccinationItem : InfoItem(isDismissible = false, hasButton = true)

        object ClockDeviationItem : InfoItem(isDismissible = false, hasButton = true)

        data class OriginExpiredItem(val greenCardType: GreenCardType, val originType: OriginType) : InfoItem(
            isDismissible = true,
            hasButton = false
        )

        object DomesticVaccinationExpiredItem: InfoItem(
            isDismissible = true,
            hasButton = true
        )

        object TestCertificate3GValidity : InfoItem(isDismissible = false, hasButton = true)

        object AppUpdate : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.recommended_update_card_action
        )

        object NewValidityItem : InfoItem(
            isDismissible = true,
            hasButton = true,
            buttonText = R.string.holder_dashboard_newvaliditybanner_action
        )

        object VisitorPassIncompleteItem : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.holder_dashboard_visitorpassincompletebanner_button_makecomplete
        )

        object BoosterItem: InfoItem(
            isDismissible = true,
            hasButton = true,
            buttonText = R.string.holder_dashboard_addBoosterBanner_button_addBooster
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