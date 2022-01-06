/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem

interface MyOverviewInfoCardItemUtil {
    fun getOriginInfoText(
        infoItem: DashboardItem.InfoItem.OriginInfoItem,
        context: Context
    ): String
}

class MyOverviewInfoCardItemUtilImpl: MyOverviewInfoCardItemUtil {
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
            is GreenCardType.Domestic -> {
                context.getString(
                    R.string.my_overview_not_valid_domestic_but_is_in_eu, originString
                )
            }
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
}