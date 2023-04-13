/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import nl.rijksoverheid.ctr.fakeCardsItem
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CardItemUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `getEnabledState returns Enabled if disclosure policy is 1G and green card has test origin`() {
        val util = getUtil()

        val getEnabledState = util.getEnabledState(
            greenCard = fakeGreenCard(
                originType = OriginType.Test,
                greenCardType = GreenCardType.Eu
            )
        )

        assertEquals(GreenCardEnabledState.Enabled, getEnabledState)
    }

    @Test
    fun `shouldDisclose returns DoNotDisclose if international green card`() {
        val cardItem = fakeCardsItem(
            greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu
            ),
            originType = OriginType.Vaccination
        ).cards.first()

        val util = getUtil()

        assertTrue(util.shouldDisclose(cardItem) is QrCodeFragmentData.ShouldDisclose.DoNotDisclose)
    }

    private fun getUtil(): CardItemUtilImpl {
        return CardItemUtilImpl()
    }
}
