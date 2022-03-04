/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.items

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class DashboardInfoCardAdapterItemUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `getOriginInfoText returns correct copy for domestic vaccination`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_domestic_but_is_in_eu, context.getString(R.string.type_vaccination))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for domestic recovery`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Recovery
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_domestic_but_is_in_eu, context.getString(R.string.type_recovery))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for domestic test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_domestic_but_is_in_eu, context.getString(R.string.type_test))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for eu vaccination`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_eu_but_is_in_domestic, context.getString(R.string.type_vaccination))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for eu recovery`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_eu_but_is_in_domestic, context.getString(R.string.type_recovery))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for eu test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Test
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.my_overview_not_valid_eu_but_is_in_domestic, context.getString(R.string.type_test))
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getOriginInfoText returns correct copy for eu vaccination assessment`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.VaccinationAssessment
        )

        val util = DashboardInfoCardAdapterItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.holder_dashboard_visitorPassInvalidOutsideNLBanner_title)
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic vaccination`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_domesticVaccine_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic recovery`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Recovery
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_domesticRecovery_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic test`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_domesticTest_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu vaccination`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_internationalVaccine_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu recovery`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_internationalRecovery_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu test`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Test
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_internationalTest_title, text)
    }

    @Test
    fun `getExpiredItemText returns correct copy for vaccination assessment`() {
        val text = DashboardInfoCardAdapterItemUtilImpl().getExpiredItemText(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.VaccinationAssessment
        )

        assertEquals(R.string.holder_dashboard_originExpiredBanner_visitorPass_title, text)
    }
}