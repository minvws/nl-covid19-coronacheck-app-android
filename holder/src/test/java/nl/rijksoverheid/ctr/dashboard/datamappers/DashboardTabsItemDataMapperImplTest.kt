/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.datamappers

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.dashboard.datamappers.DashboardTabsItemDataMapperImpl
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItems
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardTabsItemDataMapperImplTest {

    @Test
    fun `Only one international tab is returned if 0G policy`() = runBlocking {
        val cachedAppConfigUseCase = mockk<HolderCachedAppConfigUseCase>()
        coEvery { cachedAppConfigUseCase.getCachedAppConfig() } answers {
            HolderConfig.default(
                disclosurePolicy = DisclosurePolicy.ZeroG
            )
        }
        val usecase = DashboardTabsItemDataMapperImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase
        )

        val tabItems = usecase.transform(
            dashboardItems = DashboardItems(
                domesticItems = listOf(),
                internationalItems = listOf()
            )
        )

        assertEquals(1, tabItems.size)
        assertEquals(GreenCardType.Eu, tabItems.first().greenCardType)
    }
}
