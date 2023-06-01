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
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItems
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType

interface DashboardTabsItemDataMapper {
    suspend fun transform(dashboardItems: DashboardItems): List<DashboardTabItem>
}

class DashboardTabsItemDataMapperImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase
) : DashboardTabsItemDataMapper {

    override suspend fun transform(dashboardItems: DashboardItems): List<DashboardTabItem> {
        return withContext(Dispatchers.IO) {
            val tabItems = mutableListOf<DashboardTabItem>()

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
