package nl.rijksoverheid.ctr.verifier.ui.policy

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.design.utils.DialogUtilImpl
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
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

    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `given first time use and no policy selected, when click to continue, then error is shown`() {
        launchFragment()

        clickOn(R.id.confirmationButton)

        assertDisplayed(R.id.error_container)
    }

    @Test
    fun `given first time use and policy selected, when click to continue, then no error is shown, selection is stored and scanner is launched`() {
        launchFragment(
            policyState = VerificationPolicyState.Policy2G
        )

        clickOn(R.id.policy2G_container)
        clickOn(R.id.confirmationButton)

        assertNotDisplayed(R.id.error_container)
        verify { scannerUtil.launchScanner(any()) }
        verify { verificationPolicySelectionViewModel.storeSelection(VerificationPolicy.VerificationPolicy2G) }
    }

    @Test
    fun `given default policy selection fragment and stored 2g selection, when confirming 3g selection, then correct subheader and confirmation dialog show up`() {
        launchFragment(
            policyState = VerificationPolicyState.Policy2G,
            selectionType = VerificationPolicySelectionType.Default(
                ScannerState.Unlocked(
                    VerificationPolicyState.Policy2G
                )
            ),
        )

        clickOn(R.id.policy3G_container)
        clickOn(R.id.confirmationButton)
        clickOnDialogPositiveButton(R.string.verifier_risksetting_confirmation_dialog_positive_button)

        assertNotExist(
            context.getString(
                R.string.verifier_risksetting_menu_scan_settings_unselected_title,
            )
        )
        assertEquals(navController.currentDestination?.id, R.id.nav_scan_qr)
    }

    private fun clickOnDialogPositiveButton(positiveButtonTextStringId: Int) {
        onView(allOf(withId(android.R.id.button1), withText(positiveButtonTextStringId)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(ViewActions.click())
    }

    @Test
    fun `given default policy selection fragment and stored 2g selection, when confirming 2g selection, then goes to home screen`() {
        launchFragment(
            policyState = VerificationPolicyState.Policy2G,
            selectionType = VerificationPolicySelectionType.Default(
                ScannerState.Unlocked(
                    VerificationPolicyState.Policy2G
                )
            ),
        )

        clickOn(R.id.policy2G_container)
        clickOn(R.id.confirmationButton)

        assertEquals(navController.currentDestination?.id, R.id.nav_scan_qr)
    }

    @Test
    fun `given default policy selection fragment and scan not used recently, then correct subheader is showing`() {
        launchFragment(
            policyState = VerificationPolicyState.Policy2G,
            selectionType = VerificationPolicySelectionType.Default(
                ScannerState.Unlocked(
                    VerificationPolicyState.Policy2G
                )
            ),
            scannerUsedRecently = false,
        )

        assertDisplayed(
            context.getString(R.string.verifier_risksetting_menu_scan_settings_unselected_title)
        )
    }

    private fun launchFragment(
        policyState: VerificationPolicyState = VerificationPolicyState.None,
        selectionType: VerificationPolicySelectionType = VerificationPolicySelectionType.FirstTimeUse(
            ScannerState.Unlocked(policyState)
        ),
        scannerUsedRecently: Boolean = true,
    ) {

        val verificationPolicyStateUseCase =
            mockk<VerificationPolicyStateUseCase>(relaxed = true).apply {
                every { get() } returns policyState
            }

        val recentScanLogsLiveDataEvent = MutableLiveData<Event<Boolean>>()

        verificationPolicySelectionViewModel = mockk<VerificationPolicySelectionViewModel>().apply {
            coEvery { scannerUsedRecentlyLiveData } returns recentScanLogsLiveDataEvent
            coEvery { didScanRecently() } answers {
                recentScanLogsLiveDataEvent.postValue(Event(scannerUsedRecently))
            }
            coEvery { radioButtonSelected } returns when (policyState) {
                VerificationPolicyState.None -> null
                VerificationPolicyState.Policy2G -> R.id.policy2G
                VerificationPolicyState.Policy3G -> R.id.policy3G
                VerificationPolicyState.Policy2GPlus -> R.id.policy2G_plus
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

                factory {
                    DialogUtilImpl()
                }

                viewModel { verificationPolicySelectionViewModel }
            }
        )

        launchFragmentInContainer(
            bundleOf(
                "selectionType" to selectionType,
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