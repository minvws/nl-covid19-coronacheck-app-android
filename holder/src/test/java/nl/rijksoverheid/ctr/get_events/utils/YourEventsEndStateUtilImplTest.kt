/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.get_events.utils

import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.utils.StringUtil
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndState
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsEndStateUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YourEventsEndStateUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `If no hints exists return correct end state`() {
        val util = YourEventsEndStateUtilImpl(mockk())
        val endState = util.getEndState(
            context = ApplicationProvider.getApplicationContext(),
            hints = listOf()
        )
        assertEquals(YourEventsEndState.None, endState)
    }

    @Test
    fun `If negativetest_without_vaccinationassessment hint exists return correct end state`() {
        val util = YourEventsEndStateUtilImpl(mockk())
        val endState = util.getEndState(
            context = ApplicationProvider.getApplicationContext(),
            hints = listOf("negativetest_without_vaccinationassessment")
        )
        assertEquals(YourEventsEndState.AddVaccinationAssessment, endState)
    }

    @Test
    fun `If hints exists but nothing can be mapped to string resource return correct end state`() {
        val stringUtil = object : StringUtil {
            override fun getStringFromResourceName(resourceName: String): String {
                return ""
            }
        }

        val util = YourEventsEndStateUtilImpl(stringUtil)
        val endState = util.getEndState(
            context = ApplicationProvider.getApplicationContext(),
            hints = listOf("test_hint")
        )
        assertEquals(YourEventsEndState.None, endState)
    }

    @Test
    fun `If hints exists and can be mapped to string resource return correct end state`() {
        val stringUtil = object : StringUtil {
            override fun getStringFromResourceName(resourceName: String): String {
                return "Test hint"
            }
        }

        val util = YourEventsEndStateUtilImpl(stringUtil)
        val endState = util.getEndState(
            context = ApplicationProvider.getApplicationContext(),
            hints = listOf("test_hint")
        )
        assertEquals(YourEventsEndState.Hints(listOf("Test hint")), endState)
    }
}
