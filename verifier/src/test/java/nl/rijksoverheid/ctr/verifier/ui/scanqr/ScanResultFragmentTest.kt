package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.schibsted.spain.barista.assertion.BaristaAssertions.assertAny
import com.schibsted.spain.barista.assertion.BaristaBackgroundAssertions.assertHasBackground
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import nl.rijksoverheid.ctr.design.views.AbbreviatedPersonalDetailsItemWidget
import nl.rijksoverheid.ctr.design.views.AbbreviatedPersonalDetailsWidget
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeIntroductionViewModel
import nl.rijksoverheid.ctr.verifier.fakeVerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class ScanResultFragmentTest : AutoCloseKoinTest() {

    private lateinit var navController: TestNavHostController

    @Test
    fun `Valid result shows correct screen`() {
        val verifiedQr = fakeVerifiedQr(
            birthDay = "01",
            birthMonth = "01",
            firstNameInitial = "B",
            lastNameInitial = "N"
        )
        launchScanResultFragment(
            state = VerifiedQrResultState.Valid(
                verifiedQr
            )
        )
        assertHasBackground(R.id.root, R.color.green)
        assertDisplayed(R.id.title, R.string.scan_result_valid_title)
        scrollTo(R.id.subtitle)
        assertDisplayed(
            R.id.subtitle,
            InstrumentationRegistry.getInstrumentation().context.getString(R.string.scan_result_valid_subtitle)
                .fromHtml().toString()
        )

        assertAny<AbbreviatedPersonalDetailsWidget>(R.id.personalDetailsHolder) {
            val firstBox =
                (it.getChildAt(0) as AbbreviatedPersonalDetailsItemWidget).findViewById<TextView>(R.id.item_text).text
            val secondBox =
                (it.getChildAt(1) as AbbreviatedPersonalDetailsItemWidget).findViewById<TextView>(R.id.item_text).text
            val thirdBox =
                (it.getChildAt(2) as AbbreviatedPersonalDetailsItemWidget).findViewById<TextView>(R.id.item_text).text
            val fourthBox =
                (it.getChildAt(3) as AbbreviatedPersonalDetailsItemWidget).findViewById<TextView>(R.id.item_text).text

            firstBox == verifiedQr.testResultAttributes.firstNameInitial && secondBox == verifiedQr.testResultAttributes.lastNameInitial && thirdBox == verifiedQr.testResultAttributes.birthDay && fourthBox == "JAN"
        }
    }

    @Test
    fun `Valid result on description click opens explanation dialog`() {
        launchScanResultFragment()
        clickOn(R.id.subtitle)
        assertEquals(
            navController.currentDestination?.id,
            R.id.valid_explanation_bottomsheet
        )
    }

    @Test
    fun `Invalid result shows correct screen`() {
        launchScanResultFragment(state = VerifiedQrResultState.Invalid(verifiedQr = fakeVerifiedQr()))
        assertHasBackground(R.id.root, R.color.red)
        assertDisplayed(R.id.title, R.string.scan_result_invalid_title)
        scrollTo(R.id.subtitle)
        assertDisplayed(
            R.id.subtitle,
            InstrumentationRegistry.getInstrumentation().context.getString(R.string.scan_result_invalid_subtitle)
                .fromHtml().toString()
        )
    }

    @Test
    fun `Invalid result on description click opens explanation dialog`() {
        launchScanResultFragment(state = VerifiedQrResultState.Invalid(verifiedQr = fakeVerifiedQr()))
        clickOn(R.id.subtitle)
        assertEquals(
            navController.currentDestination?.id,
            R.id.invalid_explanation_bottomsheet
        )
    }

    @Test
    fun `Error result shows correct screen`() {
        launchScanResultFragment(state = VerifiedQrResultState.Error(error = "Error"))
        assertHasBackground(R.id.root, R.color.red)
        assertDisplayed(R.id.title, R.string.scan_result_invalid_title)
        scrollTo(R.id.subtitle)
        assertDisplayed(
            R.id.subtitle,
            InstrumentationRegistry.getInstrumentation().context.getString(R.string.scan_result_invalid_subtitle)
                .fromHtml().toString()
        )
    }

    @Test
    fun `Demo result shows correct screen`() {
        launchScanResultFragment(
            state = VerifiedQrResultState.Demo(verifiedQr = fakeVerifiedQr())
        )
        assertHasBackground(R.id.root, R.color.grey_medium)
        assertDisplayed(R.id.title, R.string.scan_result_demo_title)
        assertNotDisplayed(R.id.subtitle)
    }

    private fun launchScanResultFragment(
        state: VerifiedQrResultState = VerifiedQrResultState.Valid(
            fakeVerifiedQr()
        )
    ) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeIntroductionViewModel(
                        introductionFinished = true
                    )
                }
            }
        )

        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        ).also {
            it.setGraph(R.navigation.verifier_nav_graph_main)
            it.setCurrentDestination(R.id.nav_scan_result)
        }

        launchFragmentInContainer(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundleOf("validatedResult" to state)
        ) {
            ScanResultFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
