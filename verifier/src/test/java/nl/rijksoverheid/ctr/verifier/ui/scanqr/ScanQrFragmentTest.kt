package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaImageViewAssertions.assertHasDrawable
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.ui.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState
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
            scannerState = ScannerState.Unlocked(VerificationPolicySelectionState.Policy1G),
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
    fun `given 1g policy is set, 1g policy indication is shown`() {
        launchScanQrFragment(
            scannerState = ScannerState.Unlocked(VerificationPolicySelectionState.Policy1G)
        )

        assertDisplayed(R.id.indicationContainer)
        assertDisplayed(
            "1G-toegang ingesteld"
        )
    }

    @Test
    fun `given 3g policy is set, 3g policy indication is shown`() {
        launchScanQrFragment(
            scannerState = ScannerState.Unlocked(VerificationPolicySelectionState.Policy3G)
        )

        assertDisplayed(R.id.indicationContainer)
        assertDisplayed(
            "3G-toegang ingesteld"
        )
    }

    @Test
    fun `given instructions seen and policy not set, when clicking bottom button, then policy selection screen is opening`() {
        launchScanQrFragment(
            scannerNavigationState = ScannerNavigationState.VerificationPolicySelection,
        )

        clickOn(R.id.button)

        assertEquals(navController.currentDestination?.id, R.id.nav_policy_selection)
    }

    @Test
    fun `given scanner is locked in 1G, then there is 1G indication but no button`() {
        launchScanQrFragment(
            scannerState = ScannerState.Locked(5000L, VerificationPolicySelectionState.Policy1G)
        )

        assertDisplayed(
            "1G-toegang ingesteld"
        )
        assertNotDisplayed(R.id.button)
    }

    @Test
    fun `given scanner is locked in 3G, then there is 3G indication but no button`() {
        launchScanQrFragment(
            scannerState = ScannerState.Locked(5000L, VerificationPolicySelectionState.Policy3G)
        )

        assertDisplayed(
            "3G-toegang ingesteld"
        )
        assertNotDisplayed(R.id.button)
    }

    @Test
    fun `given scanner config is 1G, then the green image is shown`() {
        launchScanQrFragment(
            configVerificationPolicyState = VerificationPolicySelectionState.Policy1G
        )

        assertHasDrawable(R.id.image, R.drawable.illustration_scanner_get_started_1g)
    }

    @Test
    fun `given scanner config is 3G, then the green image is shown`() {
        launchScanQrFragment(
            configVerificationPolicyState = VerificationPolicySelectionState.Policy3G
        )

        assertHasDrawable(R.id.image, R.drawable.illustration_scanner_get_started_3g)
    }

    private fun launchScanQrFragment(
        scannerNavigationState: ScannerNavigationState? = null,
        scannerState: ScannerState = ScannerState.Unlocked(VerificationPolicySelectionState.None),
        configVerificationPolicyState: VerificationPolicySelectionState = VerificationPolicySelectionState.None,
        ) {

        val fakeScannerStateLiveData = MutableLiveData<Event<ScannerState>>()
        val fakeScannerNavigationState = MutableLiveData<Event<ScannerNavigationState>>()

        val viewModel = mockk<ScanQrViewModel>().apply {
            every { scannerStateLiveData } returns fakeScannerStateLiveData
            every { scannerNavigationStateEvent } returns fakeScannerNavigationState
            every { checkPolicyUpdate() } answers {
                fakeScannerStateLiveData.postValue(Event(scannerState))
            }
            every { nextScreen() } answers {
                scannerNavigationState?.let {
                    fakeScannerNavigationState.postValue(Event(it))
                }
            }
        }

        loadKoinModules(
            module(override = true) {
                factory {
                    scannerUtil
                }

                factory {
                    mockk<ScannerStateUseCase>().apply {
                        every { get() } returns scannerState
                    }
                }

                factory {
                    mockk<ConfigVerificationPolicyUseCase>().apply {
                        every { get() } returns configVerificationPolicyState
                    }
                }

                factory {
                    mockk< AndroidUtil>().apply {
                        every { isSmallScreen() } returns false
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
