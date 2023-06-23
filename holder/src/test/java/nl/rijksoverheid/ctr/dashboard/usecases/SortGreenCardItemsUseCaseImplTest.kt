/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.usecases

import io.mockk.mockk
import nl.rijksoverheid.ctr.fakeCardsItem
import nl.rijksoverheid.ctr.fakeCardsItems
import nl.rijksoverheid.ctr.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.usecases.SortGreenCardItemsUseCaseImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SortGreenCardItemsUseCaseImplTest : AutoCloseKoinTest() {

    private val greenCardUtil: GreenCardUtil by inject()

    @Test
    fun `Items are ordered by defined order`() {
        val items = fakeCardsItems(
            originTypes = listOf(
                OriginType.Vaccination,
                OriginType.Test,
                OriginType.Recovery
            )
        )

        val util = getUtil()

        val sortedItems = util.sort(items)

        // First item is green card with single vaccination origin
        assertEquals(
            1,
            (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.size
        )
        assertEquals(
            OriginType.Vaccination,
            (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type
        )

        // Second item is green card with single recovery origin
        assertEquals(
            1,
            (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.size
        )
        assertEquals(
            OriginType.Recovery,
            (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type
        )

        // Second item is green card with single recovery origin
        assertEquals(
            1,
            (sortedItems[2] as DashboardItem.CardsItem).cards.first().greenCard.origins.size
        )
        assertEquals(
            OriginType.Test,
            (sortedItems[2] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type
        )
    }

    @Test
    fun `Items have correct order`() {
        val items = listOf(
            DashboardItem.HeaderItem(1, null),
            DashboardItem.InfoItem.ClockDeviationItem,
            DashboardItem.InfoItem.OriginInfoItem(GreenCardType.Eu, OriginType.Test),
            DashboardItem.InfoItem.AppUpdate,
            DashboardItem.InfoItem.ConfigFreshnessWarning(1L),
            DashboardItem.AddQrButtonItem,
            DashboardItem.PlaceholderCardItem(GreenCardType.Eu),
            DashboardItem.InfoItem.GreenCardExpiredItem(GreenCardType.Eu, fakeOriginEntity()),
            fakeCardsItem()
        )

        val sortedItems = getUtil().sort(items)

        assert(sortedItems[0] is DashboardItem.HeaderItem)
        assert(sortedItems[1] is DashboardItem.InfoItem.ClockDeviationItem)
        assert(sortedItems[2] is DashboardItem.InfoItem.ConfigFreshnessWarning)
        assert(sortedItems[3] is DashboardItem.InfoItem.AppUpdate)
        assert(sortedItems[4] is DashboardItem.InfoItem.GreenCardExpiredItem)
        assert(sortedItems[5] is DashboardItem.InfoItem.OriginInfoItem)
        assert(sortedItems[6] is DashboardItem.PlaceholderCardItem)
        assert(sortedItems[7] is DashboardItem.CardsItem)
        assert(sortedItems[8] is DashboardItem.AddQrButtonItem)
    }

    private fun getUtil(): SortGreenCardItemsUseCaseImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()

        return SortGreenCardItemsUseCaseImpl(
            featureFlagUseCase = featureFlagUseCase,
            greenCardUtil = greenCardUtil
        )
    }
}
