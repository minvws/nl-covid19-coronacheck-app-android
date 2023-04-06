/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.content.Context
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

interface DashboardInfoCardAdapterItemUtil {
    fun getOriginInfoText(
        infoItem: DashboardItem.InfoItem.OriginInfoItem,
        context: Context
    ): String

    @StringRes
    fun getExpiredItemText(
        greenCardType: GreenCardType,
        originType: OriginType
    ): Int
}

class DashboardInfoCardAdapterItemUtilImpl : DashboardInfoCardAdapterItemUtil {
    override fun getOriginInfoText(
        infoItem: DashboardItem.InfoItem.OriginInfoItem,
        context: Context
    ): String {
        val originString = when (infoItem.originType) {
            is OriginType.Vaccination -> context.getString(R.string.type_vaccination)
            is OriginType.Recovery -> context.getString(R.string.type_recovery)
            is OriginType.Test -> context.getString(R.string.type_test)
            is OriginType.VaccinationAssessment -> context.getString(R.string.general_visitorPass)
        }

        return when (infoItem.greenCardType) {
            is GreenCardType.Eu -> {
                when (infoItem.originType) {
                    is OriginType.VaccinationAssessment -> {
                        context.getString(R.string.holder_dashboard_visitorPassInvalidOutsideNLBanner_title)
                    }
                    else -> {
                        context.getString(
                            R.string.my_overview_not_valid_eu_but_is_in_domestic, originString
                        )
                    }
                }
            }
        }
    }

    override fun getExpiredItemText(
        greenCardType: GreenCardType,
        originType: OriginType
    ): Int {
        return when {
            greenCardType == GreenCardType.Eu && originType == OriginType.Vaccination -> R.string.holder_dashboard_originExpiredBanner_internationalVaccine_title
            greenCardType == GreenCardType.Eu && originType == OriginType.Recovery -> R.string.holder_dashboard_originExpiredBanner_internationalRecovery_title
            greenCardType == GreenCardType.Eu && originType == OriginType.Test -> R.string.holder_dashboard_originExpiredBanner_internationalTest_title
            originType == OriginType.VaccinationAssessment -> R.string.holder_dashboard_originExpiredBanner_visitorPass_title
            else -> R.string.qr_card_expired
        }
    }
}
