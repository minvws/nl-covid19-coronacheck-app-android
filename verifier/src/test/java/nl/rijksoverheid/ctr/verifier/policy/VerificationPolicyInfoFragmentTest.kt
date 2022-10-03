package nl.rijksoverheid.ctr.verifier.policy

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.policy.VerificationPolicySelectionState.Policy1G
import nl.rijksoverheid.ctr.verifier.policy.VerificationPolicySelectionState.Policy3G
import nl.rijksoverheid.ctr.verifier.usecases.ScannerStateUseCase
import nl.rijksoverheid.ctr.verifier.usecases.VerifierFeatureFlagUseCase
import org.junit.Test
import org.junit.runner.RunWith
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
class VerificationPolicyInfoFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.verifier_nav_graph_main)
        it.setCurrentDestination(R.id.nav_scan_qr)
    }

    @Test
    fun `given no policy is set, policy related views not displayed`() {
        launchFragment()

        assertNotDisplayed(R.id.separator1)
        assertNotDisplayed(R.id.policySettingHeader)
        assertNotDisplayed(R.id.policySettingBody)
        assertDisplayed(R.id.bottom)
    }

    @Test
    fun `given 1g policy is set, 1g policy related views are displayed`() {
        launchFragment(
            verificationPolicySelectionState = Policy1G
        )

        assertDisplayed(R.id.separator1)
        assertDisplayed(
            R.id.policySettingHeader,
            "1G-toegang ingesteld"
        )
        assertDisplayed(R.id.policySettingBody, R.string.verifier_risksetting_subtitle_1G)
        assertNotDisplayed(R.id.bottom)
    }

    @Test
    fun `given 3g policy is set, 3g policy related views are displayed`() {
        launchFragment(
            verificationPolicySelectionState = Policy3G
        )

        assertDisplayed(R.id.separator1)
        assertDisplayed(
            R.id.policySettingHeader,
            "3G-toegang ingesteld"
        )
        assertDisplayed(R.id.policySettingBody, R.string.verifier_risksetting_subtitle_3G)
        assertNotDisplayed(R.id.bottom)
    }

    private fun launchFragment(
        verificationPolicySelectionState: VerificationPolicySelectionState = VerificationPolicySelectionState.Selection.None,
        isVerificationPolicySelectionEnabled: Boolean = true
    ) {
        loadKoinModules(
            module(override = true) {
                factory {
                    mockk<ScannerStateUseCase>().apply {
                        every { get() } returns ScannerState.Unlocked(verificationPolicySelectionState)
                    }
                }
                factory {
                    mockk<VerifierFeatureFlagUseCase>().apply {
                        every { isVerificationPolicySelectionEnabled() } answers { isVerificationPolicySelectionEnabled }
                    }
                }
            }
        )

        launchFragmentInContainer(
            themeResId = R.style.AppTheme
        ) {
            VerificationPolicyInfoFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
