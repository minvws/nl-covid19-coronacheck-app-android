package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerUtil
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
        it.setGraph(R.navigation.verifier_nav_graph_main)
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

    /**
     * Camera qr code scanner is bypassed in test
     */
    @Test
    fun `Clicking start scan and scanning valid qr goes to valid scan result`() {
        launchScanQrFragment()
        clickOn(R.id.button)
        assertEquals(R.id.nav_scan_result, navController.currentDestination?.id)
        assertEquals(
            VerifiedQrResultState.Valid(fakeVerifiedQr()),
            navController.backStack.last().arguments?.get("validatedResult")
        )
    }

    /**
     * Camera qr code scanner is bypassed in test
     */
    @Test
    fun `Clicking start scan and scanning invalid qr goes to invalid scan result`() {
        launchScanQrFragment(state = VerifiedQrResultState.Invalid(verifiedQr = fakeVerifiedQr()))
        clickOn(R.id.button)
        assertEquals(R.id.nav_scan_result, navController.currentDestination?.id)
        assertEquals(
            VerifiedQrResultState.Invalid(verifiedQr = fakeVerifiedQr()),
            navController.backStack.last().arguments?.get("validatedResult")
        )
    }

    private fun launchScanQrFragment(
        state: VerifiedQrResultState = VerifiedQrResultState.Valid(
            fakeVerifiedQr()
        ),
        hasSeenScanInstructions: Boolean = true
    ) {
        loadKoinModules(
            module(override = true) {
                factory<SharedPreferences> {
                    PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().context)
                }
                factory<QrCodeScannerUtil> {
                    object : QrCodeScannerUtil {
                        override fun launchScanner(
                            activity: Activity,
                            activityResultLauncher: ActivityResultLauncher<Intent>,
                            customTitle: String,
                            customMessage: String,
                            rationaleDialogTitle: String?,
                            rationaleDialogDescription: String?,
                            rationaleDialogOkayButtonText: String?
                        ) {
                            navController.navigate(ScanQrFragmentDirections.actionScanResult(state))
                        }

                        override fun createQrCode(
                            qrCodeContent: String,
                            width: Int,
                            height: Int
                        ): Bitmap {
                            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        }

                        override fun parseScanResult(resultIntent: Intent?): String? {
                            return null
                        }
                    }
                }

                viewModel {
                    fakeScanQrViewModel(
                        result = state,
                        scanInstructionsSeen = hasSeenScanInstructions
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
