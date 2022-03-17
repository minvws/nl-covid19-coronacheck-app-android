/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Assert
import org.junit.Test

class DashboardHeaderAdapterItemUtilImplTest {

    @Test
    fun `Correct copy for domestic tab and empty state with policy 1G`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneG
        )

        Assert.assertEquals(R.string.holder_dashboard_empty_domestic_only1Gaccess_message,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = true,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct copy for domestic tab with empty state with policy 3G`() {
        val util = getUtil(
            policy = DisclosurePolicy.ThreeG
        )

        Assert.assertEquals(R.string.my_overview_qr_placeholder_description,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = true,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct copy for domestic tab with empty state with policy 1G,3G`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneAndThreeG
        )

        Assert.assertEquals(R.string.holder_dashboard_empty_domestic_3Gand1Gaccess_message,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = true,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct copy for domestic tab with non empty state with policy 1G`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneG
        )

        Assert.assertEquals(R.string.holder_dashboard_intro_domestic_only1Gaccess,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = false,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct copy for domestic tab with non empty state with policy 3G`() {
        val util = getUtil(
            policy = DisclosurePolicy.ThreeG
        )

        Assert.assertEquals(R.string.my_overview_description,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = false,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct copy for domestic tab with non empty state with policy 1G,3G`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneAndThreeG
        )

        Assert.assertEquals(R.string.holder_dashboard_intro_domestic_3Gand1Gaccess,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = false,
                hasVisitorPassIncompleteItem = false
            ).text
        )
    }

    @Test
    fun `Correct button info for domestic tab`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneG
        )

        Assert.assertEquals(null,
            util.getHeaderItem(
                tabType = GreenCardType.Domestic,
                emptyState = true,
                hasVisitorPassIncompleteItem = false
            ).buttonInfo
        )
    }

    @Test
    fun `Correct button info for EU tab with 0G and not empty`() {
        val util = getUtil(
            policy = DisclosurePolicy.ZeroG
        )

        val headerItem = util.getHeaderItem(
            tabType = GreenCardType.Eu,
            emptyState = false,
            hasVisitorPassIncompleteItem = false
        )

        Assert.assertEquals(
            R.string.my_overview_description_eu_button_text,
            headerItem.buttonInfo!!.text
        )
        Assert.assertEquals(
            R.string.my_overview_description_eu_button_link,
            headerItem.buttonInfo!!.link
        )
    }

    @Test
    fun `Correct button info for EU tab with 0G and empty`() {
        val util = getUtil(
            policy = DisclosurePolicy.ZeroG
        )

        val headerItem = util.getHeaderItem(
            tabType = GreenCardType.Eu,
            emptyState = true,
            hasVisitorPassIncompleteItem = false
        )

        Assert.assertEquals(
            R.string.holder_dashboard_international_0G_action_certificateNeeded,
            headerItem.buttonInfo!!.text
        )
        Assert.assertEquals(
            R.string.my_overview_description_eu_button_link,
            headerItem.buttonInfo!!.link
        )
    }

    @Test
    fun `Correct button info for EU tab with not 0G`() {
        val util = getUtil(
            policy = DisclosurePolicy.OneG
        )

        val headerItem = util.getHeaderItem(
            tabType = GreenCardType.Eu,
            emptyState = false,
            hasVisitorPassIncompleteItem = false
        )

        Assert.assertEquals(
            R.string.my_overview_description_eu_button_text,
            headerItem.buttonInfo!!.text
        )
        Assert.assertEquals(
            R.string.my_overview_description_eu_button_link,
            headerItem.buttonInfo!!.link
        )
    }

    private fun getUtil(policy: DisclosurePolicy): DashboardHeaderAdapterItemUtilImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()
        every { featureFlagUseCase.getDisclosurePolicy() } answers { policy }

        return DashboardHeaderAdapterItemUtilImpl(
            featureFlagUseCase = featureFlagUseCase
        )
    }
}