/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.fakeCardsItem
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CardItemUtilImplTest : AutoCloseKoinTest() {

    private val greenCardUtil: GreenCardUtil by inject()

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 1G and green card is in domestic tab and green card has vaccination origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Vaccination,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 1G if disclosure policy is 1G and green card is in domestic tab and green card has test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.OneG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 3G and green card is in domestic tab and green card has vaccination origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Vaccination,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 3G and green card is in domestic tab and green card has test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 1G and green card is in eu tab`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Vaccination,
                greenCardType = GreenCardType.Eu
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 3G-1G and green card is in domestic tab and first index`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 1G if disclosure policy is 3G-1G and green card is in domestic tab and second index`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCardDisclosurePolicy = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Domestic
            ),
            greenCardIndex = 1
        )

        assertEquals(GreenCardDisclosurePolicy.OneG, greenCardDisclosurePolicy)
    }

    @Test
    fun `getDisclosurePolicy returns 1G if disclosure policy is 1G and green card is in domestic tab and green card has all test origins`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCardMultipleTestOrigins = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic
            ).copy(
                origins = listOf(
                    fakeOriginEntity(type = OriginType.Test),
                    fakeOriginEntity(type = OriginType.Test),
                    fakeOriginEntity(type = OriginType.Test)
                )
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.OneG, greenCardMultipleTestOrigins)
    }

    @Test
    fun `getDisclosurePolicy returns 3G if disclosure policy is 1G and green card is in domestic tab and green card has multiple origin types`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCardMultipleOrigins = util.getDisclosurePolicy(
            greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic
            ).copy(
                origins = listOf(
                    fakeOriginEntity(type = OriginType.Vaccination),
                    fakeOriginEntity(type = OriginType.Recovery)
                )
            ),
            greenCardIndex = 0
        )

        assertEquals(GreenCardDisclosurePolicy.ThreeG, greenCardMultipleOrigins)
    }

    @Test
    fun `getEnabledState returns Enabled if disclosure policy is 1G and green card has test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val getEnabledState = util.getEnabledState(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Domestic
            )
        )

        assertEquals(GreenCardEnabledState.Enabled, getEnabledState)
    }

    @Test
    fun `getEnabledState returns Disabled if disclosure policy is 1G and green card has vaccination origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val getEnabledState = util.getEnabledState(
            greenCard = fakeGreenCard(
                originType = OriginType.Vaccination,
                greenCardType = GreenCardType.Domestic
            )
        )

        assertEquals(GreenCardEnabledState.Disabled(), getEnabledState)
    }

    @Test
    fun `getEnabledState returns Enabled if disclosure policy is 3G and green card has vaccination origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        val getEnabledState = util.getEnabledState(
            greenCard = fakeGreenCard(
                originType = OriginType.Vaccination,
                greenCardType = GreenCardType.Domestic
            )
        )

        assertEquals(GreenCardEnabledState.Enabled, getEnabledState)
    }

    @Test
    fun `shouldDisclose returns Disclose if domestic green card`() {
        val cardItem = fakeCardsItem(
            greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic
            ),
            originType = OriginType.Vaccination
        ).cards.first()

        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        assertTrue(util.shouldDisclose(cardItem) is QrCodeFragmentData.ShouldDisclose.Disclose)
    }

    @Test
    fun `shouldDisclose returns DoNotDisclose if international green card`() {
        val cardItem = fakeCardsItem(
            greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu
            ),
            originType = OriginType.Vaccination
        ).cards.first()

        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        assertTrue(util.shouldDisclose(cardItem) is QrCodeFragmentData.ShouldDisclose.DoNotDisclose)
    }

    @Test
    fun `title in OneThreeG indicates if card is 1G or 3G`() {
        val util = getUtil(DisclosurePolicy.OneAndThreeG)

        val actualFor1GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.OneG
        })

        val actualFor3GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.ThreeG
        })

        assertEquals(R.string.holder_showQR_domestic_title_1g, actualFor1GCard)
        assertEquals(R.string.holder_showQR_domestic_title_3g, actualFor3GCard)
    }

    @Test
    fun `title in OneG indicates if card is 1G or 3G`() {
        val util = getUtil(DisclosurePolicy.OneG)

        val actualFor1GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.OneG
        })

        val actualFor3GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.ThreeG
        })

        assertEquals(R.string.holder_showQR_domestic_title_1g, actualFor1GCard)
        assertEquals(R.string.holder_showQR_domestic_title_3g, actualFor3GCard)
    }

    @Test
    fun `title in 0G is always the default one`() {
        val util = getUtil(DisclosurePolicy.ZeroG)

        val actualFor1GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.OneG
        })

        val actualFor3GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.ThreeG
        })

        assertEquals(R.string.domestic_qr_code_title, actualFor1GCard)
        assertEquals(R.string.domestic_qr_code_title, actualFor3GCard)
    }

    @Test
    fun `title in 3G is always the default one`() {
        val util = getUtil(DisclosurePolicy.ThreeG)

        val actualFor1GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.OneG
        })

        val actualFor3GCard = util.getQrCodesFragmentToolbarTitle(mockk<DashboardItem.CardsItem.CardItem>().apply {
            every { disclosurePolicy } returns GreenCardDisclosurePolicy.ThreeG
        })

        assertEquals(R.string.domestic_qr_code_title, actualFor1GCard)
        assertEquals(R.string.domestic_qr_code_title, actualFor3GCard)
    }

    private fun getUtil(
        disclosurePolicy: DisclosurePolicy
    ): CardItemUtilImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()
        every { featureFlagUseCase.getDisclosurePolicy() } answers { disclosurePolicy }

        return CardItemUtilImpl(
            featureFlagUseCase = featureFlagUseCase,
            greenCardUtil = greenCardUtil
        )
    }
}
