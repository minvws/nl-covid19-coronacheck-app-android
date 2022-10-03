/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.your_events.widgets

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.assertEquals
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtilImpl
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YourEventWidgetUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `getVaccinationEventTitle returns correct title if dcc`() {
        val util = YourEventWidgetUtilImpl()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val vaccination = mockk<RemoteEventVaccination>()
        every { vaccination.vaccination?.doseNumber } answers { "2" }
        every { vaccination.vaccination?.totalDoses } answers { "2" }

        val title = util.getVaccinationEventTitle(
            context = context,
            isDccEvent = true,
            currentEvent = vaccination
        )

        val expectedTitle = context.getString(R.string.retrieved_vaccination_dcc_title, "2", "2")
        assertEquals(expectedTitle, title)
    }

    @Test
    fun `getVaccinationEventTitle returns correct title if not dcc`() {
        val util = YourEventWidgetUtilImpl()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val vaccination = mockk<RemoteEventVaccination>()
        every { vaccination.vaccination?.date } answers { LocalDate.of(1970, 1, 1) }

        val title = util.getVaccinationEventTitle(
            context = context,
            isDccEvent = false,
            currentEvent = vaccination
        )

        val expectedTitle = context.getString(R.string.retrieved_vaccination_title)
        assertEquals(expectedTitle, title)
    }

    @Test
    fun `getVaccinationEventSubtitle returns correct subtitle if dcc`() {
        val util = YourEventWidgetUtilImpl()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val subtitle = util.getVaccinationEventSubtitle(
            context = context,
            isDccEvent = true,
            providerIdentifiers = "",
            vaccinationDate = "29 Juni 2022",
            fullName = "de Bouwer, Bob",
            birthDate = "1 Januari 1970"
        )

        val expectedSubtitle = context.getString(R.string.your_vaccination_dcc_row_subtitle, "29 Juni 2022", "de Bouwer, Bob", "1 Januari 1970")
        assertEquals(expectedSubtitle, subtitle)
    }

    @Test
    fun `getVaccinationEventSubtitle returns correct subtitle if non dcc`() {
        val util = YourEventWidgetUtilImpl()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val subtitle = util.getVaccinationEventSubtitle(
            context = context,
            isDccEvent = false,
            providerIdentifiers = "GGD",
            vaccinationDate = "29 Juni 2022",
            fullName = "de Bouwer, Bob",
            birthDate = "1 Januari 1970"
        )

        val expectedSubtitle = context.getString(R.string.your_vaccination_row_subtitle, "29 Juni 2022", "de Bouwer, Bob", "1 Januari 1970", "GGD")
        assertEquals(expectedSubtitle, subtitle)
    }
}
