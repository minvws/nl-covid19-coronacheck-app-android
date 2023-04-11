/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import org.junit.Assert
import org.junit.Test

class DashboardHeaderAdapterItemUtilImplTest {

    @Test
    fun `Correct button info for EU tab with 0G and not empty`() {
        val util = getUtil()

        val headerItem = util.getHeaderItem(
            greenCardType = GreenCardType.Eu,
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
        val util = getUtil()

        val headerItem = util.getHeaderItem(
            greenCardType = GreenCardType.Eu,
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
        val util = getUtil()

        val headerItem = util.getHeaderItem(
            greenCardType = GreenCardType.Eu,
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

    private fun getUtil(): DashboardHeaderAdapterItemUtilImpl {
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()

        return DashboardHeaderAdapterItemUtilImpl(
            featureFlagUseCase = featureFlagUseCase
        )
    }
}
