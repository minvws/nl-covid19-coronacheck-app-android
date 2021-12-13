package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.*
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.junit.Rule
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
class VerificationPolicySelectionFragmentTest : AutoCloseKoinTest() {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val scannerUtil = mockk<ScannerUtil>(relaxed = true)
    private lateinit var verificationPolicySelectionViewModel: VerificationPolicySelectionViewModel

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
        launchFragment(
            policyState = VerificationPolicyState.Policy2G
        )

        clickOn(R.id.policy2G_container)
        clickOn(R.id.confirmationButton)

        assertNotDisplayed(R.id.error_container)
        verify { scannerUtil.launchScanner(any()) }
        verify { verificationPolicySelectionViewModel.storeSelection(VerificationPolicy.VerificationPolicy2G) }
    }

    private fun launchFragment(
        policyState: VerificationPolicyState = VerificationPolicyState.None,
    ) {

        val verificationPolicyStateUseCase = mockk<VerificationPolicyStateUseCase>(relaxed = true).apply {
            every { get() } returns policyState
        }

        val recentScanLogsLiveDataEvent = MutableLiveData<Event<Boolean>>()

        verificationPolicySelectionViewModel =  mockk<VerificationPolicySelectionViewModel>().apply {
            coEvery { scannerUsedRecentlyLiveData } returns recentScanLogsLiveDataEvent
            coEvery { didScanRecently() }  answers {
                recentScanLogsLiveDataEvent.postValue(Event(true))
            }
            coEvery { radioButtonSelected } returns when (policyState) {
                VerificationPolicyState.None -> null
                VerificationPolicyState.Policy2G -> R.id.policy2G
                VerificationPolicyState.Policy3G -> R.id.policy3G
            }
            coEvery { updateRadioButton(any()) } returns Unit
            coEvery { storeSelection(any()) } returns Unit
        }

        loadKoinModules(
            module(override = true) {
                factory {
                    scannerUtil
                }

                factory {
                    verificationPolicyStateUseCase
                }


                viewModel { verificationPolicySelectionViewModel }
            }
        )

        launchFragmentInContainer(
            bundleOf(
                "selectionType" to VerificationPolicySelectionType.FirstTimeUse(ScannerState.Unlocked(policyState)),
                "toolbarTitle" to "Scan settings",
            ), themeResId = R.style.AppTheme
        ) {
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