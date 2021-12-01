package nl.rijksoverheid.ctr.verifier.ui.policy

import android.content.SharedPreferences
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.interaction.BaristaClickInteractions
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeScanQrViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanqr.NextScannerScreenState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class VerificationPolicySelectionFragmentTest {

    private val scannerUtil: ScannerUtil = mockk(relaxed = true)
    private val persistenceManager: PersistenceManager = mockk(relaxed = true)

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.verifier_nav_graph_main)
        it.setCurrentDestination(R.id.nav_scan_qr)
    }

    @Test
    fun `given no policy selected, when click to continue, then error is shown`() {
        launchFragment()

        clickOn(R.id.confirmationButton)

        assertDisplayed(R.id.error_container)
    }

    @Test
    fun `given policy selected, when click to continue, then error is shown`() {
        launchFragment()

        clickOn(R.id.policy2G)
        clickOn(R.id.confirmationButton)

        assertNotDisplayed(R.id.error_container)
        verify { scannerUtil.launchScanner(any()) }
    }

    private fun launchFragment() {
        loadKoinModules(
            module(override = true) {
                factory {
                    persistenceManager
                }

                factory {
                    scannerUtil
                }
            }
        )

        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            VerificationPolicySelectionFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}