/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.fakeCardsItem
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
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
class CardItemUtilImplTest: AutoCloseKoinTest() {

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