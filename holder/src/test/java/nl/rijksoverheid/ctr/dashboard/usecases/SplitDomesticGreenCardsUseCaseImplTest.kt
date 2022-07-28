/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.usecases

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.fakeGreenCardWithOrigins
import nl.rijksoverheid.ctr.holder.dashboard.usecases.SplitDomesticGreenCardsUseCaseImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SplitDomesticGreenCardsUseCaseImplTest : AutoCloseKoinTest() {

    private val greenCardUtil: GreenCardUtil by inject()

    @Test
    fun `getSplittedDomesticGreenCards splits a new green card with test origin on 1G policy set with test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery, OriginType.Test)
        )

        val splittedGreenCards = util.getSplitDomesticGreenCards(
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

        val splittedGreenCards = util.getSplitDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splittedGreenCards.size)
        assertEquals(2, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
    }

    @Test
    fun `getSplittedDomesticGreenCards splits a new green card with test origin on 1G-3G policy set with test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Vaccination, OriginType.Recovery, OriginType.Test)
        )

        val splittedGreenCards = util.getSplitDomesticGreenCards(
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
    fun `getSplittedDomesticGreenCards duplicates green card with test origin on 1G-3G policy set with single test origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneAndThreeG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Test)
        )

        val splittedGreenCards = util.getSplitDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(2, splittedGreenCards.size)
        assertEquals(1, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Test, splittedGreenCards.first().origins.first().type)
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

        val splittedGreenCards = util.getSplitDomesticGreenCards(
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

        val splittedGreenCards = util.getSplitDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splittedGreenCards.size)
        assertEquals(3, splittedGreenCards.first().origins.size)
        assertEquals(OriginType.Vaccination, splittedGreenCards.first().origins.first().type)
        assertEquals(OriginType.Recovery, splittedGreenCards.first().origins[1].type)
        assertEquals(OriginType.Test, splittedGreenCards.first().origins[2].type)
    }

    @Test
    fun `getSplitDomesticGreenCards does not split green card when there is only one origin`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Test)
        )

        val splitGreenCards = util.getSplitDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splitGreenCards.size)
    }

    @Test
    fun `getSplitDomesticGreenCards does not split green card when it's all test origins`() {
        val util = getUtil(
            disclosurePolicy = DisclosurePolicy.OneG
        )

        val greenCard = fakeGreenCardWithOrigins(
            originTypes = listOf(OriginType.Test, OriginType.Test)
        )

        val splitGreenCards = util.getSplitDomesticGreenCards(
            domesticGreenCards = listOf(greenCard)
        )

        assertEquals(1, splitGreenCards.size)
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
