/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeGreenCard
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
class MyOverviewInfoCardItemUtilImplTest: AutoCloseKoinTest() {

    @Test
    fun `getOriginInfoText returns correct copy for domestic vaccination`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val infoItem = DashboardItem.InfoItem.OriginInfoItem(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
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

        val util = MyOverviewInfoCardItemUtilImpl()
        val copy = util.getOriginInfoText(
            context = context,
            infoItem = infoItem,
        )

        val expectedCopy = context.getString(R.string.holder_dashboard_visitorPassInvalidOutsideNLBanner_title)
        assertEquals(expectedCopy, copy)
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic vaccination`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_domesticVaccine_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic recovery`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Recovery
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_domesticRecovery_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for domestic test`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_domesticTest_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu vaccination`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_internationalVaccine_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu recovery`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_internationalRecovery_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for eu test`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Test
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_internationalTest_title
        )
    }

    @Test
    fun `getExpiredItemText returns correct copy for vaccination assessment`() {
        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.VaccinationAssessment
        )
        val expiredItem = DashboardItem.InfoItem.GreenCardExpiredItem(greenCard)

        assertEquals(
            MyOverviewInfoCardItemUtilImpl().getExpiredItemText(expiredItem),
            R.string.holder_dashboard_originExpiredBanner_visitorPass_title
        )
    }
}