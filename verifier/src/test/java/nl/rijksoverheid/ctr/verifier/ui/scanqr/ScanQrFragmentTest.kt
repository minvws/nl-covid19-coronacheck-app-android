package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeIntroductionViewModel
import nl.rijksoverheid.ctr.verifier.fakeScanQrViewModel
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
class ScanQrFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.verifier_nav_graph)
        it.setCurrentDestination(R.id.nav_scan_qr)
    }

    @Test
    fun `Clicking description navigates to scan instructions`() {
        launchScanQrFragment()
        clickOn(R.id.description)
        assertEquals(navController.currentDestination?.id, R.id.nav_scan_instructions)
    }

    @Test
    fun `First time clicking start scan first opens scan instructions`() {
        launchScanQrFragment(
            hasSeenScanInstructions = false
        )
        clickOn(R.id.button)
        assertEquals(navController.currentDestination?.id, R.id.nav_scan_instructions)
    }

    private fun launchScanQrFragment(
        state: VerifiedQrResultState = VerifiedQrResultState.Valid(
            fakeVerifiedQr
        ),
        hasSeenScanInstructions: Boolean = true
    ) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeScanQrViewModel(
                        result = state,
                        scanInstructionsSeen = hasSeenScanInstructions
                    )
                }
                viewModel {
                    fakeIntroductionViewModel(
                        introductionFinished = true
                    )
                }
            }
        )

        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            ScanQrFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

}
