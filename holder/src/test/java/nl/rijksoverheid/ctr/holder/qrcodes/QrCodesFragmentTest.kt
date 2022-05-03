package nl.rijksoverheid.ctr.holder.qrcodes

import android.graphics.Bitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import com.adevinta.android.barista.interaction.BaristaViewPagerInteractions.swipeViewPagerBack
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeMobileCoreWrapper
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodeUtil
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.OffsetDateTime


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-rNL-w412dp-h732dp-xhdpi")
class QrCodesFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_qr_codes)
    }

    private fun qrCodeFragmentData(
        expiredQrCode: Boolean
    ): QrCodeFragmentData {
        return QrCodeFragmentData(
            type = GreenCardType.Eu,
            originType = OriginType.Vaccination,
            credentials = listOf("".toByteArray(), "".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.DoNotDisclose,
            credentialExpirationTimeSeconds = OffsetDateTime.now()
                .toEpochSecond() + if (expiredQrCode) {
                -100000
            } else {
                100000
            },
        )
    }

    private fun launch(
        expiredQrCode: Boolean
    ) {
        loadKoinModules(
            module(override = true) {
                factory<QrCodeUtil> {
                    object : QrCodeUtil {
                        override fun createQrCode(
                            qrCodeContent: String,
                            width: Int,
                            height: Int,
                            errorCorrectionLevel: ErrorCorrectionLevel
                        ) = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565)
                    }
                }
                factory {
                    fakeMobileCoreWrapper()
                }
            }
        )
        launchFragmentInContainer(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundleOf(
                "toolbarTitle" to "International QR",
                "returnUri" to null,
                "data" to qrCodeFragmentData(expiredQrCode)
            )
        ) {
            QrCodesFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun `expired dcc qr code is hidden and shows expired subtitle`() {
        launch(expiredQrCode = true)

        swipeViewPagerBack()

        assertDisplayed(R.id.overlay)
        assertDisplayed(R.string.holder_showQR_label_expiredVaccination)
    }

    @Test
    fun `not expired dcc qr code has no overlay and shows dosis subtitle`() {
        launch(expiredQrCode = false)

        swipeViewPagerBack()

        assertNotDisplayed(R.id.overlay)
        assertNotExist(R.string.qr_code_newer_dose_available)
    }
}