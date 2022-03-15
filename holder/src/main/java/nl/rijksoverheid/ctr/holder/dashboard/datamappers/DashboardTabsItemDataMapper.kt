/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.datamappers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItems
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DashboardTabsItemDataMapper {
    suspend fun transform(dashboardItems: DashboardItems): List<DashboardTabItem>
}

class DashboardTabsItemDataMapperImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
): DashboardTabsItemDataMapper {

    override suspend fun transform(dashboardItems: DashboardItems): List<DashboardTabItem> {
        return withContext(Dispatchers.IO) {
            val tabItems = mutableListOf<DashboardTabItem>()

            if (cachedAppConfigUseCase.getCachedAppConfig().disclosurePolicy !is DisclosurePolicy.ZeroG) {
                val domesticItem = DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Domestic,
                    items = dashboardItems.domesticItems
                )

                tabItems.add(domesticItem)
            }

            val internationalItem = DashboardTabItem(
                title = R.string.travel_button_europe,
                greenCardType = GreenCardType.Eu,
                items = dashboardItems.internationalItems
            )
            tabItems.add(internationalItem)

            tabItems
        }
    }
}