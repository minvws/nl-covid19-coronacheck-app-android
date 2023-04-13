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
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard

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

        object ClockDeviationItem : InfoItem(isDismissible = false, hasButton = true)

        data class GreenCardExpiredItem(
            val greenCardType: GreenCardType,
            val originEntity: OriginEntity
        ) : InfoItem(
            isDismissible = true,
            hasButton = false
        )

        object AppUpdate : InfoItem(
            isDismissible = false,
            hasButton = true,
            buttonText = R.string.recommended_update_card_action
        )

        data class BlockedEvents(
            val blockedEvents: List<RemovedEventEntity>,
            @StringRes override val buttonText: Int = R.string.general_readmore
        ) : InfoItem(isDismissible = true, hasButton = true)

        data class FuzzyMatchedEvents(
            val storedEvent: EventGroupEntity,
            val events: List<RemovedEventEntity>,
            @StringRes override val buttonText: Int = R.string.general_readmore
        ) : InfoItem(isDismissible = true, hasButton = true)
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
            val databaseSyncerResult: DatabaseSyncerResult,
            val greenCardEnabledState: GreenCardEnabledState
        )
    }

    object AddQrButtonItem : DashboardItem()

    object AddQrCardItem : DashboardItem()
}
