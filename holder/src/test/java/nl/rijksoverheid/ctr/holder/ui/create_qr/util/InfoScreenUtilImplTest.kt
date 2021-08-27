/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeCachedAppConfigUseCase
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class InfoScreenUtilImplTest: AutoCloseKoinTest() {


    private val infoScreenUtil =
        InfoScreenUtilImpl(ApplicationProvider.getApplicationContext(), mockk(), fakeCachedAppConfigUseCase())

    @Test
    fun `getCountry returns correct strings for the Netherlands in Dutch locale`() {
        val dutchString = infoScreenUtil.getCountry("NL", Locale("nl", "nl"))
        assertEquals("Nederland / The Netherlands", dutchString)
    }

    @Test
    fun `getCountry returns correct strings for Belgium in Dutch locale`() {
        val belgianString = infoScreenUtil.getCountry("be", Locale("nl", "nl"))
        assertEquals("België / Belgium", belgianString)
    }

    @Test
    fun `getCountry returns correct strings for the Netherlands in English locale`() {
        val dutchString = infoScreenUtil.getCountry("nl", Locale("en", "en"))
        assertEquals("Netherlands", dutchString)
    }

    @Test
    fun `getForEuropeanTestQr returns correct info`() {
        val jsonString = "{\"credentialVersion\":1,\"issuer\":\"NL\",\"issuedAt\":1628579448,\"expirationTime\":1630998658,\"dcc\":{\"ver\":\"1.3.0\",\"dob\":\"1991-08-18\",\"nam\":{\"fn\":\"ten Bouwer\",\"fnt\":\"TEN<BOUWER\",\"gn\":\"Bob\",\"gnt\":\"BOB\"},\"v\":null,\"t\":[{\"tg\":\"840539006\",\"tt\":\"LP6464-4\",\"nm\":\"\",\"ma\":\"\",\"sc\":\"2021-08-10T03:10:00+00:00\",\"dr\":\"\",\"tr\":\"260415000\",\"tc\":\"Facility approved by the State of The Netherlands\",\"co\":\"NL\",\"is\":\"Ministry of Health Welfare and Sport\",\"ci\":\"URN:UCI:01:NL:PJ7JLSZ4KRGX5O2E7OD342#E\"}],\"r\":null}}"
        val json = JSONObject(jsonString)

        val infoScreen = infoScreenUtil.getForEuropeanTestQr(json)

        assertEquals((ApplicationProvider.getApplicationContext() as Context).getString(R.string.qr_explanation_description_eu_test,
            "ten Bouwer, Bob",
            "18-08-1991",
            "COVID-19",
            "LP6464-4",
            "",
            "10-08-2021",
            "negatief (geen corona)",
            "Facility approved by the State of The Netherlands",
            "",
            "Netherlands",
            "Ministerie van VWS / Ministry of Health, Welfare and Sport",
            "URN:UCI:01:NL:PJ7JLSZ4KRGX5O2E7OD342#E"
            ), infoScreen.description)
    }

}