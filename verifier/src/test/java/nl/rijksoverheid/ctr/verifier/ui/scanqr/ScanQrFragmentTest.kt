package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeScanQrViewModel
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class ScanQrFragmentTest : AutoCloseKoinTest() {

    private val scannerUtil: ScannerUtil = mockk(relaxed = true)

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.verifier_nav_graph_main)
        it.setCurrentDestination(R.id.nav_scan_qr)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `Clicking instruction button navigates to scan instructions`() {
        launchScanQrFragment()
        clickOn(R.id.instructionsButton)
        assertEquals(navController.currentDestination?.id, R.id.nav_scan_instructions)
    }

    @Test
    fun `First time clicking start scan first opens scan instructions`() {
        launchScanQrFragment(
            hasSeenScanInstructions = false,
            scannerNavigationState = ScannerNavigationState.Instructions
        )
        clickOn(R.id.button)
        assertEquals(navController.currentDestination?.id, R.id.nav_scan_instructions)
    }

    /**
     * Camera qr code scanner is bypassed in test
     */
    @Test
    fun `Given instructions seen and policy set, Clicking start scan opens scanner`() {
        launchScanQrFragment(
            policyState = VerificationPolicyState.Policy2G,
            scannerNavigationState = ScannerNavigationState.Scanner(false),
        )
        clickOn(R.id.button)
        verify { scannerUtil.launchScanner(any()) }
    }

    @Test
    fun `given no policy is set, no policy indication is shown`() {
        launchScanQrFragment()

        assertNotDisplayed(R.id.indicationContainer)
    }

    @Test
    fun `given 2g policy is set, 2g policy indication is shown`() {
        launchScanQrFragment(
            policyState = VerificationPolicyState.Policy2G
        )

        assertDisplayed(R.id.indicationContainer)
        assertDisplayed(R.id.policyIndicatorText, R.string.verifier_start_scan_qr_policy_indication_2g)
    }

    @Test
    fun `given 3g policy is set, 3g policy indication is shown`() {
        launchScanQrFragment(
            policyState = VerificationPolicyState.Policy3G
        )

        assertDisplayed(R.id.indicationContainer)
        assertDisplayed(R.id.policyIndicatorText, R.string.verifier_start_scan_qr_policy_indication_3g)
    }

    private fun launchScanQrFragment(
        hasSeenScanInstructions: Boolean = true,
        scannerNavigationState: ScannerNavigationState? = null,
        policyState: VerificationPolicyState = VerificationPolicyState.None,
    ) {

        val viewModel = fakeScanQrViewModel(
            scanInstructionsSeen = hasSeenScanInstructions,
            scannerNavigationState = scannerNavigationState ?: ScannerNavigationState.Scanner(false),
        )

        (viewModel.liveData as MutableLiveData).postValue(
            Event(
                ScannerState.Unlocked(policyState)
            )
        )

        scannerNavigationState?.let {
            (viewModel.startupStateEvent as MutableLiveData).postValue(Event(it))
        }

        loadKoinModules(
            module(override = true) {
                factory<SharedPreferences> {
                    PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().context)
                }

                factory {
                    scannerUtil
                }

                factory {
                    mockk<PersistenceManager>().apply {
                        every { getLastScanLockTimeSeconds() } returns 0L
                    }
                }

                factory {
                    mockk<VerificationPolicyUseCase>(relaxed = true)
                }
                
                factory {
                    mockk<ScannerStateUseCase>().apply { 
                        every { get() } returns ScannerState.Unlocked(policyState)
                    }
                }

                viewModel {
                    viewModel
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
