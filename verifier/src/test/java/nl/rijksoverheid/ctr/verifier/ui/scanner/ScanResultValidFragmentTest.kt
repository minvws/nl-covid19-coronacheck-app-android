package nl.rijksoverheid.ctr.verifier.ui.scanner

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaBackgroundAssertions.assertHasBackground
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.fakeVerifiedQr
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultValidData
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
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
class ScanResultValidFragmentTest : AutoCloseKoinTest() {

    private lateinit var navController: TestNavHostController
    private val scannerUtil: ScannerUtil = mockk(relaxed = true)

    @Test
    fun `Screen shows correct content when data is Valid`() {
        launchScanResultValidFragment(
            data = ScanResultValidData.Valid(
                verifiedQr = fakeVerifiedQr(),
                null
            )
        )
        assertHasBackground(R.id.root, R.color.secondary_green)
        assertDisplayed(R.id.title, R.string.scan_result_valid_title)
    }

    @Test
    fun `Screen shows correct content when data is Demo`() {
        launchScanResultValidFragment(
            data = ScanResultValidData.Demo(
                verifiedQr = fakeVerifiedQr(),
                null
            )
        )
        assertHasBackground(R.id.root, R.color.grey_2)
        assertDisplayed(R.id.title, R.string.scan_result_demo_title)
    }

    private fun launchScanResultValidFragment(
        data: ScanResultValidData = ScanResultValidData.Valid(
            fakeVerifiedQr(),
            null
        )
    ) {
        loadKoinModules(
            module(override = true) {
                factory {
                    scannerUtil
                }
            }
        )

        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        ).also {
            it.setGraph(R.navigation.verifier_nav_graph_scanner)
            it.setCurrentDestination(R.id.nav_scan_result_valid)
        }

        launchFragmentInContainer(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundleOf("validData" to data)
        ) {
            ScanResultValidFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
