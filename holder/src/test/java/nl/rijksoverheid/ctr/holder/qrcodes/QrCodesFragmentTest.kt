package nl.rijksoverheid.ctr.holder.qrcodes

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import com.adevinta.android.barista.interaction.BaristaViewPagerInteractions.swipeViewPagerBack
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeMobileCoreWrapper
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Ignore
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
@Ignore("these tests work if run the class tests but fail for some reason when run all module's tests together")
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
        val qrCodeContent =
            "HC1:NCFOXN%TSMAHN-HKTGX94G-ICWEXWP769W1O3XH74M6R57E9+6N6NRTPII*VPV5-FJLF6CB9YPD.+IKYJ1A4DBCEF3JTC 5T8MS*XC9NDF0D*JC10067T\$2JE%50OPG989B92FF9B9LW4G%8Z*8CNNK1H7/GVD9H:OD4OYGFO-O/HL.KJ C1TGL0LOYGFDB5*95MKN4NN3F85QN\$24:O1\$R1 SI5K1*TB3:U-1VVS1UU1\$%HFTIPPAAMI PQVW5/O16%HAT1Z%PHOP+MMBT16Y5+Z9XV7G+SI*VQBKCY0Z44ON1UVI/E2\$4JY/K+.S+2T%:KW/S8JVR+3\$BJ.+I92K70ULOJ1ALJYJAZI-3C ZJ83B7N2*EU:H3N6E N3\$9T5-IZ0K%PIUY25HTS SR633WSNYJF0JEYI1DLZZL162964HUGY3LEGJR4NI:5/M99+RE5G65N8XFKBVU7LSXBKIJ\$7O/5IH.MN%J3S6W.ESVGB7KVR1/VT26MEW9NMS\$EF*:QOJU9AKP/P\$77\$*0NDB\$UF"
        return QrCodeFragmentData(
            type = GreenCardType.Eu,
            originType = OriginType.Vaccination,
            credentials = listOf(qrCodeContent.toByteArray(), qrCodeContent.toByteArray()),
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
    fun `not expired dcc qr code has no overlay and doesn't show expired subtitle`() {
        launch(expiredQrCode = false)

        swipeViewPagerBack()

        assertNotExist(R.id.overlay)
        assertNotExist(R.string.holder_showQR_label_expiredVaccination)
    }
}