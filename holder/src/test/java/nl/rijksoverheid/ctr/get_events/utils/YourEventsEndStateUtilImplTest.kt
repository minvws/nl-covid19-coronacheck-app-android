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
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndStateWithCustomTitle
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsEndStateUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private fun getEndState(hints: List<String>): YourEventsEndState {
        val util = YourEventsEndStateUtilImpl(mockk(relaxed = true))
        return util.getEndState(
            context = ApplicationProvider.getApplicationContext(),
            hints = hints
        )
    }

    @Test
    fun `additional vaccination endstates`() {
        // Vaccinations with/without positive tests
        /* 000 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_created",
                    "international_vaccination_created"
                )
            ), YourEventsEndState.None
        )
        /* 001 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_created"
                )
            ), YourEventsEndStateWithCustomTitle.InternationalQROnly
        )
        /* 002 */ assertTrue(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_rejected"
                )
            ) is YourEventsEndState.WeCouldntMakeACertificateError
        )
        /* 003 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_created",
                    "international_vaccination_created",
                    "vaccination_dose_correction_applied",
                    "domestic_recovery_rejected",
                    "international_recovery_rejected"
                )
            ), YourEventsEndState.None
        )
        /* 004 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_created",
                    "international_vaccination_created",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_rejected",
                    "international_recovery_rejected"
                )
            ), YourEventsEndState.None
        )
        /* 005 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_created",
                    "international_vaccination_created",
                    "vaccination_dose_correction_applied",
                    "domestic_recovery_created",
                    "international_recovery_created"
                )
            ), YourEventsEndStateWithCustomTitle.VaccinationsAndRecovery
        )
        /* 006 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_created",
                    "international_vaccination_created",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_created",
                    "international_recovery_created"
                )
            ), YourEventsEndStateWithCustomTitle.VaccinationsAndRecovery
        )
        /* 007 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_created",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_created",
                    "international_recovery_created"
                )
            ), YourEventsEndStateWithCustomTitle.InternationalVaccinationAndRecovery
        )
        /* 008 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_created",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_rejected",
                    "international_recovery_rejected"
                )
            ), YourEventsEndStateWithCustomTitle.InternationalQROnly
        )
        /* 009 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_rejected",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_created",
                    "international_recovery_created"
                )
            ), YourEventsEndStateWithCustomTitle.RecoveryOnly
        )
        /* 010 */ assertTrue(
            getEndState(
                listOf(
                    "domestic_vaccination_rejected",
                    "international_vaccination_rejected",
                    "vaccination_dose_correction_not_applied",
                    "domestic_recovery_rejected",
                    "international_recovery_rejected"
                )
            ) is YourEventsEndState.WeCouldntMakeACertificateError
        )
    }

    @Test
    fun `additional recovery endstates`() {
        // Positive tests only
        /* 011 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_created",
                    "international_recovery_created"
                )
            ), YourEventsEndState.None
        )
        /* 012 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_created",
                    "international_recovery_rejected"
                )
            ), YourEventsEndState.None
        )
        /* 013 */ assertTrue(
            getEndState(
                listOf(
                    "domestic_recovery_rejected",
                    "international_recovery_rejected"
                )
            ) is YourEventsEndState.WeCouldntMakeACertificateError
        )
        /* 014 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_created",
                    "international_recovery_created",
                    "vaccination_dose_correction_not_applied"
                )
            ), YourEventsEndState.None
        )
        /* 015 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_created",
                    "international_recovery_created",
                    "vaccination_dose_correction_applied"
                )
            ), YourEventsEndStateWithCustomTitle.RecoveryAndDosisCorrection
        )
        /* 016 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_rejected",
                    "international_recovery_rejected",
                    "vaccination_dose_correction_applied"
                )
            ), YourEventsEndStateWithCustomTitle.NoRecoveryButDosisCorrection
        )
        /* 017 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_recovery_rejected",
                    "international_recovery_rejected",
                    "international_recovery_too_old"
                )
            ), YourEventsEndStateWithCustomTitle.RecoveryTooOld
        )
    }

    @Test
    fun `additional negative test endstates`() {
        // Negative tests
        /* 018 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_negativetest_created",
                    "international_negativetest_created"
                )
            ), YourEventsEndState.None
        )
        /* 019 */ assertEquals(
            getEndState(
                listOf(
                    "domestic_negativetest_rejected",
                    "international_negativetest_created"
                )
            ), YourEventsEndState.None
        )
        /* 020 - skip?*/
        /* 021 */ assertTrue(
            getEndState(
                listOf(
                    "domestic_negativetest_rejected",
                    "international_negativetest_rejected"
                )
            ) is YourEventsEndState.WeCouldntMakeACertificateError
        )
    }
}
