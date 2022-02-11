/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.fakeGreenCardWithOrigins
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SplitDomesticGreenCardsUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SplitDomesticGreenCardsUseCaseImplTest: AutoCloseKoinTest() {

    private val greenCardUtil: GreenCardUtil by inject()

    @Test
    fun `getSplittedDomesticGreenCards splits green card by test origin on 1G policy set with test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery, OriginType.Test)
        )

        val splittedGreenCards = util.getSplittedDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(2, splittedGreenCards.size)
        assertEquals(2, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
        assertEquals(1, splittedGreenCards[1].origins.size)
        assertEquals(OriginType.Test, splittedGreenCards[1].origins.first().type)
    }

    @Test
    fun `getSplittedDomesticGreenCards does not split green card by test origin on 1G policy set and no test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery)
        )

        val splittedGreenCards = util.getSplittedDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splittedGreenCards.size)
        assertEquals(2, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
    }

    @Test
    fun `getSplittedDomesticGreenCards splits green card by test origin on 1G-3G policy set with test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery, OriginType.Test)
        )

        val splittedGreenCards = util.getSplittedDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(2, splittedGreenCards.size)
        assertEquals(2, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
        assertEquals(1, splittedGreenCards[1].origins.size)
        assertEquals(OriginType.Test, splittedGreenCards[1].origins.first().type)
    }

    @Test
    fun `getSplittedDomesticGreenCards does not split green card by test origin on 1G-3G policy set and no test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery)
        )

        val splittedGreenCards = util.getSplittedDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splittedGreenCards.size)
        assertEquals(2, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
    }

    @Test
    fun `getSplittedDomesticGreenCards does not split green card by test origin on 3G policy set`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery, OriginType.Test)
        )

        val splittedGreenCards = util.getSplittedDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splittedGreenCards.size)
        assertEquals(3, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
        assertEquals(OriginType.Test, splittedGreenCards.first().origins[2].type)
    }

    private fun getUtil(
        disclosurePolicy: DisclosurePolicy
    ): SplitDomesticGreenCardsUseCaseImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()
        every { featureFlagUseCase.getDisclosurePolicy() } answers { disclosurePolicy }

        return SplitDomesticGreenCardsUseCaseImpl(
            featureFlagUseCase = featureFlagUseCase,
            greenCardUtil = greenCardUtil
        )
    }
}