package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState.*
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase
import org.junit.Test
import org.junit.runner.RunWith
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
        assertNotDisplayed(R.id.separator2)
        assertNotDisplayed(R.id.policySettingHeader)
        assertNotDisplayed(R.id.policySettingBody)
        assertDisplayed(R.id.bottom)
    }

    @Test
    fun `given 2g policy is set, 2g policy related views are displayed`() {
        launchFragment(
            verificationPolicySelectionState = Policy1G
        )

        assertDisplayed(R.id.separator1)
        assertDisplayed(R.id.separator2)
        assertDisplayed(
            R.id.policySettingHeader,
            R.string.verifier_start_scan_qr_policy_indication_2g
        )
        assertDisplayed(R.id.policySettingBody, R.string.verifier_risksetting_highrisk_subtitle)
        assertNotDisplayed(R.id.bottom)
    }

    @Test
    fun `given 3g policy is set, 3g policy related views are displayed`() {
        launchFragment(
            verificationPolicySelectionState = Policy3G
        )

        assertDisplayed(R.id.separator1)
        assertDisplayed(R.id.separator2)
        assertDisplayed(
            R.id.policySettingHeader,
            R.string.verifier_start_scan_qr_policy_indication_3g
        )
        assertDisplayed(R.id.policySettingBody, R.string.verifier_risksetting_lowrisk_subtitle)
        assertNotDisplayed(R.id.bottom)
    }

    private fun launchFragment(
        verificationPolicySelectionState: VerificationPolicySelectionState = None,
        isVerificationPolicySelectionEnabled: Boolean = true,
    ) {
        loadKoinModules(
            module(override = true) {
                factory {
                    mockk<ScannerStateUseCase>().apply {
                        every { get() } returns ScannerState.Unlocked(verificationPolicySelectionState)
                    }
                }
                factory {
                    mockk<FeatureFlagUseCase>().apply {
                        every { isVerificationPolicyEnabled() } answers { true }
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