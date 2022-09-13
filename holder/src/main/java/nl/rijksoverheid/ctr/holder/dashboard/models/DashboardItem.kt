/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.ButtonInfo
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy

sealed class DashboardItem {

    data class HeaderItem(
        @StringRes val text: Int,
        val buttonInfo: ButtonInfo?
    ) : DashboardItem()

    data class PlaceholderCardItem(val greenCardType: GreenCardType) : DashboardItem()

    sealed class InfoItem(
        val isDismissible: Boolean,
        val hasButton: Boolean,
        @StringRes open val buttonText: Int? = null
    ) : DashboardItem() {

        data class ConfigFreshnessWarning(val maxValidityDate: Long) :
            InfoItem(isDismissible = false, hasButton = true)

        data class OriginInfoItem(
            val greenCardType: GreenCardType,
            val originType: OriginType
        ) : InfoItem(isDismissible = false, hasButton = true)

        object MissingDutchVaccinationItem : InfoItem(isDismissible = false, hasButton = true)

        object ClockDeviationItem : InfoItem(isDismissible = false, hasButton = true)

        data class GreenCardExpiredItem(
            val greenCardType: GreenCardType,
            val originEntity: OriginEntity
        ) : InfoItem(
            isDismissible = true,
            hasButton = false
        )

        data class DomesticVaccinationExpiredItem(val originEntity: OriginEntity) : InfoItem(
            isDismissible = true,
            hasButton = true
        )

        data class DomesticVaccinationAssessmentExpiredItem(val originEntity: OriginEntity) :
            InfoItem(
                isDismissible = true,
                hasButton = true
            )

        object AppUpdate : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.recommended_update_card_action
        )

        object VisitorPassIncompleteItem : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.holder_dashboard_visitorpassincompletebanner_button_makecomplete
        )

        data class DisclosurePolicyItem(
            val disclosurePolicy: DisclosurePolicy,
            @StringRes override val buttonText: Int = R.string.general_readmore
        ) :
            InfoItem(
                isDismissible = true,
                hasButton = true
            )

        data class BlockedEvents(
            val blockedEvents: List<BlockedEventEntity>,
            @StringRes override val buttonText: Int = R.string.general_readmore
        ) : InfoItem(isDismissible = true, hasButton = true)
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
            val databaseSyncerResult: DatabaseSyncerResult,
            val disclosurePolicy: GreenCardDisclosurePolicy,
            val greenCardEnabledState: GreenCardEnabledState
        )
    }

    object AddQrButtonItem : DashboardItem()

    object AddQrCardItem : DashboardItem()
}
