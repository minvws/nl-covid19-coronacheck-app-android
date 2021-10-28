package nl.rijksoverheid.ctr.verifier.ui.scanner

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.button.MaterialButton
import com.schibsted.spain.barista.assertion.BaristaAssertions.assertAny
import com.schibsted.spain.barista.assertion.BaristaBackgroundAssertions.assertHasBackground
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import com.schibsted.spain.barista.internal.performActionOnView
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeVerifiedQr
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultInvalidData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
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
class ScanResultInvalidFragmentTest : AutoCloseKoinTest() {

    private lateinit var navController: TestNavHostController

    @Test
    fun `Screen shows correct content`() {
        launchScanResultInvalidFragment(data = ScanResultInvalidData.Error("invalid QR code"))
        assertHasBackground(R.id.root, R.color.red)
        scrollTo(R.id.title)
        assertDisplayed(R.id.title, R.string.scan_result_invalid_title)
        assertAny<MaterialButton>(R.id.secondaryButton) {
            it.text == InstrumentationRegistry.getInstrumentation().context.getString(
                R.string.scan_result_invalid_explanation_button
            )
        }
    }

    @Test
    fun `Invalid result on explanation button click opens explanation dialog`() {
        launchScanResultInvalidFragment(data = ScanResultInvalidData.Error("invalid QR code"))
        performActionOnView(withId(R.id.secondaryButton), click())
        assertContains(R.string.scan_result_invalid_reason_title)
    }

    @Test
    fun `DCC QR issued in NL shows correct error message`() {
        launchScanResultInvalidFragment(data = ScanResultInvalidData.Invalid(verifiedQr = fakeVerifiedQr()))
        assertHasBackground(R.id.root, R.color.red)
        scrollTo(R.id.title)
        assertDisplayed(R.id.title, R.string.scan_result_european_nl_invalid_title)
    }

    @Test
    fun `Clicking scan again button opens scanner`() {
        launchScanResultInvalidFragment()
        clickOn(R.id.button)
        assertEquals(navController.currentDestination?.id, R.id.nav_qr_scanner)
    }


    private fun launchScanResultInvalidFragment(
        data: ScanResultInvalidData = ScanResultInvalidData.Invalid(
            fakeVerifiedQr()
        )
    ) {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        ).also {
            it.setGraph(R.navigation.verifier_nav_graph_scanner)
            it.setCurrentDestination(R.id.nav_scan_result_invalid)
        }

        launchFragmentInContainer(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundleOf("invalidData" to data)
        ) {
            ScanResultInvalidFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
