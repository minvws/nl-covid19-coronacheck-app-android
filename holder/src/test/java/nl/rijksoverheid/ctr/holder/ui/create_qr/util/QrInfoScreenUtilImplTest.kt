/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert.assertEquals
import nl.rijksoverheid.ctr.holder.fakeCachedAppConfigUseCase
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl")
class QrInfoScreenUtilImplTest : AutoCloseKoinTest() {

    private val infoScreenUtil =
        QrInfoScreenUtilImpl(
            ApplicationProvider.getApplicationContext(),
            ReadEuropeanCredentialUtilImpl(
                ApplicationProvider.getApplicationContext()
            ),
            CountryUtilImpl(),
            fakeCachedAppConfigUseCase(),
        )

    @Test
    fun `getForEuropeanTestQr returns correct info`() {
        val jsonString =
            "{\"credentialVersion\":1,\"issuer\":\"NL\",\"issuedAt\":1628579448,\"expirationTime\":1630998658,\"dcc\":{\"ver\":\"1.3.0\",\"dob\":\"1991-08-18\",\"nam\":{\"fn\":\"ten Bouwer\",\"fnt\":\"TEN<BOUWER\",\"gn\":\"Bob\",\"gnt\":\"BOB\"},\"v\":null,\"t\":[{\"tg\":\"840539006\",\"tt\":\"LP6464-4\",\"nm\":\"\",\"ma\":\"\",\"sc\":\"2021-08-10T03:10:00+00:00\",\"dr\":\"\",\"tr\":\"260415000\",\"tc\":\"Facility approved by the State of The Netherlands\",\"co\":\"NL\",\"is\":\"Ministry of Health Welfare and Sport\",\"ci\":\"URN:UCI:01:NL:PJ7JLSZ4KRGX5O2E7OD342#E\"}],\"r\":null}}"
        val json = JSONObject(jsonString)

        val infoScreen = infoScreenUtil.getForEuropeanTestQr(json)

        assertEquals(
            "Ben je in het buitenland of ga je de grens over? Dan zijn er meer gegevens nodig dan in Nederland. Daarom staan in jouw internationale QR-code de volgende gegevens:<br/><br/>Naam / Name:<br/><b>ten Bouwer, Bob</b><br/><br/>Geboortedatum / Date of birth*:<br/><b>18-08-1991</b><br/><br/>Ziekteverwekker / Disease targeted:<br/><b>COVID-19</b><br/><br/>Type test / Type of test:<br/><b>LP6464-4</b><br/><br/>Test naam / Test name:<br/><b></b><br/><br/>Testdatum / Test date*:<br/><b>10-08-2021</b><br/><br/>Testuitslag / Test result:<br/><b>Negatief (geen corona)</b><br/><br/>Testlocatie / Testing centre:<br/><b>Facility approved by the State of The Netherlands</b><br/><br/>Producent / Test manufacturer:<br/><b></b><br/><br/>Getest in / Member state of test:<br/><b>Netherlands</b><br/><br/>Afgever certificaat / Certificate issuer:<br/><b>Ministerie van VWS / Ministry of Health, Welfare and Sport</b><br/><br/>Uniek certificaatnummer / Unique certificate identifier:<br/><b>URN:UCI:01:NL:PJ7JLSZ4KRGX5O2E7OD342#E</b><br/><br/>",
            infoScreen.description
        )
        assertEquals(
            "*Datum weergegeven in dag-maand-jaar / Date noted in day-month-year.",
            infoScreen.footer
        )
    }

}