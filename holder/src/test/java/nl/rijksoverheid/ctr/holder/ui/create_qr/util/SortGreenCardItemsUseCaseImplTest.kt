/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.fakeCardsItems
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SortGreenCardItemsUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SortGreenCardItemsUseCaseImplTest: AutoCloseKoinTest() {

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

        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        val sortedItems = util.sort(items)

        // First item is green card with single vaccination origin
        assertEquals(1, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Vaccination, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)

        // Second item is green card with single recovery origin
        assertEquals(1, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Recovery, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)

        // Second item is green card with single recovery origin
        assertEquals(1, (sortedItems[2] as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Test, (sortedItems[2] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)
    }

    @Test
    fun `Domestic green card with single test origin is always on top if policy is set to 1G`() {
        val items = fakeCardsItems(
            originTypes = listOf(
                OriginType.Vaccination,
                OriginType.Test
            )
        )

        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val sortedItems = util.sort(items)

        // First item is green card with single test origin
        assertEquals(1, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Test, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)

        // Second item is green card with single vaccination origin
        assertEquals(1, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Vaccination, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)
    }

    @Test
    fun `Domestic green card with single test origin is always on bottom if policy is set to 1G-3G`() {
        val items = fakeCardsItems(
            originTypes = listOf(
                OriginType.Vaccination,
                OriginType.Test
            )
        )

        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val sortedItems = util.sort(items)

        // First item is green card with single vaccination origin
        assertEquals(1, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Vaccination, (sortedItems.first() as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)

        // Second item is green card with single test origin
        assertEquals(1, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.size)
        assertEquals(OriginType.Test, (sortedItems[1] as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type)
    }

    private fun getUtil(
        disclosurePolicy: DisclosurePolicy
    ): SortGreenCardItemsUseCaseImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()
        every { featureFlagUseCase.getDisclosurePolicy() } answers { disclosurePolicy }

        return SortGreenCardItemsUseCaseImpl()
    }
}