package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.mockk
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ReadEuropeanCredentialUtilImplTest {

    private val clock = Clock.fixed(Instant.ofEpochSecond(1609498800), ZoneId.of("UTC")) // 2021-01-01
    private val util = ReadEuropeanCredentialUtilImpl(mockk(), clock)

    @Test
    fun `Vaccination should be hidden if it's older than 25 days and of lower dose than maximum`() {
        val hidden = getVaccinationJson("2020-12-06", dose = "1", ofTotalDoses = "2")
        val notHiddenBecauseOfDate = getVaccinationJson("2021-01-01", dose = "1", ofTotalDoses = "2")
        val notHiddenBecauseOfDose = getVaccinationJson("2020-12-01", dose = "2", ofTotalDoses = "2")

        assertTrue(util.vaccinationShouldBeHidden(hidden))
        assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDate))
        assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDose))
    }

    private fun getVaccinationJson(date: String, dose: String = "2", ofTotalDoses: String = "2") =
        JSONObject(
            "{\n" +
                    "    \"credentialVersion\": 1,\n" +
                    "    \"issuer\": \"NL\",\n" +
                    "    \"issuedAt\": 1626174495,\n" +
                    "    \"expirationTime\": 1628753641,\n" +
                    "    \"dcc\": {\n" +
                    "        \"ver\": \"1.3.0\",\n" +
                    "        \"dob\": \"1950-02-01\",\n" +
                    "        \"nam\": {\n" +
                    "            \"fn\": \"Pricks Same Brand\",\n" +
                    "            \"fnt\": \"PRICKS<SAME<BRAND\",\n" +
                    "            \"gn\": \"Two\",\n" +
                    "            \"gnt\": \"TWO\"\n" +
                    "        },\n" +
                    "        \"v\": [\n" +
                    "            {\n" +
                    "                \"tg\": \"840539006\",\n" +
                    "                \"vp\": \"1119349007\",\n" +
                    "                \"mp\": \"EU\\/1\\/20\\/1528\",\n" +
                    "                \"ma\": \"ORG-100030215\",\n" +
                    "                \"dn\": \"$dose\",\n" +
                    "                \"sd\": \"$ofTotalDoses\",\n" +
                    "                \"dt\": \"$date\",\n" +
                    "                \"co\": \"NL\",\n" +
                    "                \"is\": \"Ministry of Health Welfare and Sport\",\n" +
                    "                \"ci\": \"URN:UCI:01:NL:IZES3LGRDVDPVIHYKPOE42#\\/\"\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"t\": null,\n" +
                    "        \"r\": null\n" +
                    "    }\n" +
                    "}"
        )
}