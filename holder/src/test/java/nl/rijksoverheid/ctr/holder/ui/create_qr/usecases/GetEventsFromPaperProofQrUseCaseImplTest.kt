package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails

@RunWith(RobolectricTestRunner::class)
class GetEventsFromPaperProofQrUseCaseImplTest : AutoCloseKoinTest() {

    private val remoteEventUtil: RemoteEventUtil by inject()
    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)

    private val useCase = GetEventsFromPaperProofQrUseCaseImpl(mobileCoreWrapper, remoteEventUtil)

    @Test
    fun `vaccination should be parsed from qr`() {
        val vaccinationQr =
            "HC1:NCFO20\$80T9WTWGVLK-49NJ3B0J\$OCC*AX*4FBB.R3*70J+9DN03E52F3%0US.3Y50.FK8ZKO/EZKEZ967L6C56GVC*JC1A6QW63W5KF6746TPCBEC7ZKW.CSEE*KEQPC.OEFOAF\$DN34VKE0/DLPCG/DSEE5IA\$M8NNASNAQY9 R7.HAB+9 JC:.DNUAU3EI3D5WE TAQ1A7:EDOL9WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46.JCP9EJY8L/5M/5546.96VF6.JCBECB1A-:8\$966469L6OF6VX6FVCPD0KQEPD0LVC6JD846Y96D463W5307UPCBJCOT9+EDL8FHZ95/D QEALEN44:+C%69AECAWE:34: CJ.CZKE9440/D+34S9E5LEWJC0FD3%4AIA%G7ZM81G72A6J+9QG7OIBENA.S90IAY+A17A+B9:CB*6AVX8AF6F:5678M2927SM6NAN24WKP0VTMO8.CMJF1CF-*7%XN3R0C0E45L0EKUGEA-SL0HYN71PBTWHCITDHPIHG/A7%8U9PEBHEPD9DD4\$O4000FGW5HIWGG"
        val credential = getVaccinationJson()

        every {
            hint(JSONObject::class)
            mobileCoreWrapper.readEuropeanCredential(vaccinationQr.toByteArray())
        } returns credential

        with(useCase.get(vaccinationQr)) {
            assertEquals(providerIdentifier, "dcc")
            assertEquals(protocolVersion, "3.0")
            assertEquals(status, RemoteProtocol.Status.COMPLETE)
            assertEquals(holder!!.infix, "")
            assertEquals(holder!!.firstName, "Two")
            assertEquals(holder!!.lastName, "Pricks Same Brand")
            assertEquals(holder!!.birthDate, "1950-02-01")
            with(events!!.first() as RemoteEventVaccination) {
                assertEquals(type, "vaccination")
                assertEquals(unique, "URN:UCI:01:NL:IZES3LGRDVDPVIHYKPOE42#/")
                assertEquals(vaccination!!.doseNumber, "2")
                assertEquals(vaccination!!.totalDoses, "2")
                assertEquals(vaccination!!.date, LocalDate.parse("2021-07-07"))
                assertEquals(vaccination!!.country, "NL")
                assertEquals(vaccination!!.type, "1119349007")
                assertEquals(vaccination!!.brand, "EU/1/20/1528")
                assertEquals(vaccination!!.manufacturer, "ORG-100030215")
            }
        }
    }

    @Test
    fun `recovery should be parsed from qr`() {
        val recoveryQr =
            "HC1:NCF%RN%TS3DH0RGPJB/IB-OM7533SR*BH9M9*VIHWF S4KHRFH2SZ9OUM:UC*GP-S4FT5D75W9AAABE34+V4YC5/HQ/ PHCR+9AFDOEA7IB65C94JB11L0PL:OA1FD\$JDOKEH-BK2L.UL4TIXADMPD9JAW/B:OA1JA6LFBE9NUIGOA%FAGUU0QIRR97I2HOAXL92L0: KQMK8J4RK46YB9M65QC2%KI*V.18N\$K-PSJY2W*PP+P8OI.I9Y*VSV0I+QWZJAQ12KUL JS%O\$UA9*OXQ29HS9.VAOI5XIKXC-B5P54NB27FCYE9*FJRLDQC8\$.AJ5QH*AA:G8/FV+AM8WJ1E8CA0D89.B40LL5OS\$4AGCZ3SW.89/H%Y1W1W*\$UAY5LYQ735\$CF\$YR7YMACPX\$3F:O0MBE4LSB2+E7XVJ-OKYO3T6IA6E/TN:T2JL3N0JK1KEL4COE8DQ%5KK1LS.RR3SRB3*50000FGW DP+ME"
        val credential = getRecoveryJson()

        every {
            hint(JSONObject::class)
            mobileCoreWrapper.readEuropeanCredential(recoveryQr.toByteArray())
        } returns credential

        with(useCase.get(recoveryQr)) {
            assertEquals(providerIdentifier, "dcc")
            assertEquals(protocolVersion, "3.0")
            assertEquals(status, RemoteProtocol.Status.COMPLETE)
            assertEquals(holder!!.infix, "")
            assertEquals(holder!!.firstName, "Bob")
            assertEquals(holder!!.lastName, "De Bouwer")
            assertEquals(holder!!.birthDate, "1960-01-01")
            with(events!!.first() as RemoteEventRecovery) {
                assertEquals(type, "recovery")
                assertEquals(unique, "URN:UCI:01:NL:ROIFOZLRYJF5TMC4RV3K42#A")
                assertEquals(isSpecimen, false)
                assertEquals(recovery!!.sampleDate, LocalDate.parse("2021-06-30"))
                assertEquals(recovery!!.validFrom, LocalDate.parse("2021-06-30"))
                assertEquals(recovery!!.validUntil, LocalDate.parse("2022-01-09"))
            }
        }
    }

    @Test
    fun `test should be parsed from qr`() {
        val testQr =
            "HC1:NCF%RN%TS3DH0RGPJB/IB-OM7533SR7694RI3XH8/FWP5IJBVGAMAU5PNPF6R:5SVBWVBDKBYLDZ4D74DWZJ\$7K+ CREDRCK*9C%PD8DJI7JSTNB95326HW4*IOQAOGU7\$35+Y5MT4K0P*5PP:7X\$RL353X7IKRE:7SA7G6M/NRO9SQKMHEE5IAXMFU*GSHGRKMXGG6DB-B93:GQBGZHHBIH5C9HFEC+GYHILIIX2MELNJIKCCHWIJNKMQ-ILKLXGGN+IRB84C9Q2LCIJ/HHKGL/BHOUB7IT8DJUIJ6DBSJLI7BI8AZ3CVOJ3BI9IL NILMLSVB*8BEPLA8KC42UIIUHSBKB+GIAZI3DJ/JAJZIR9KICT.XI/VB6TSYIJGDBGIA181:0TLOJJPACGKC2KRTI-8BEPL3DJ/LKQVBE2C*NIKYJIGK:H3J1DKVTQEDK8C+2TDSCNTCNJS6F3W.C\$USE\$2:*TIT3C7D8MS7LCTO3MMSSHT0\$U58PLY3 ZRA5PUF7MDN QKI7B\$WKL 6Q:S14GW4Q:LRERC6FPK1J*IUIH7S3J UQ2VQQ3ONV2CVR/TFFSQJ8KP.BENIQETGK6112U50-BW/IVK5"
        val credential = getTestJson()

        every {
            hint(JSONObject::class)
            mobileCoreWrapper.readEuropeanCredential(testQr.toByteArray())
        } returns credential

        with(useCase.get(testQr)) {
            assertEquals(providerIdentifier, "dcc")
            assertEquals(protocolVersion, "3.0")
            assertEquals(status, RemoteProtocol.Status.COMPLETE)
            assertEquals(holder!!.infix, "")
            assertEquals(holder!!.firstName, "Bob")
            assertEquals(holder!!.lastName, "De Bouwer")
            assertEquals(holder!!.birthDate, "1960-01-01")
            with(events!!.first() as RemoteEventNegativeTest) {
                assertEquals(type, "test")
                assertEquals(unique, "URN:UCI:01:NL:B3PEER674NFX3C3VA3XD42#P")
                assertEquals(isSpecimen, false)
                assertEquals(
                    negativeTest!!.sampleDate,
                    OffsetDateTime.parse("2021-07-13T16:31:35+00:00")
                )
                assertEquals(negativeTest!!.negativeResult, true)
                assertEquals(
                    negativeTest!!.facility,
                    "Facility approved by the State of The Netherlands"
                )
                assertEquals(negativeTest!!.type, "LP6464-4")
                assertEquals(negativeTest!!.name, "name")
                assertEquals(negativeTest!!.manufacturer, "manufacturer")
            }
        }
    }

    @Test
    fun `test qr with empty name and manufacturer should parse to null`() {
        val testQr =
            "HC1:NCF%RN%TS3DH0RGPJB/IB-OM7533SR7694RI3XH8/FWP5IJBVGAMAU5PNPF6R:5SVBWVBDKBYLDZ4D74DWZJ\$7K+ CREDRCK*9C%PD8DJI7JSTNB95326HW4*IOQAOGU7\$35+Y5MT4K0P*5PP:7X\$RL353X7IKRE:7SA7G6M/NRO9SQKMHEE5IAXMFU*GSHGRKMXGG6DB-B93:GQBGZHHBIH5C9HFEC+GYHILIIX2MELNJIKCCHWIJNKMQ-ILKLXGGN+IRB84C9Q2LCIJ/HHKGL/BHOUB7IT8DJUIJ6DBSJLI7BI8AZ3CVOJ3BI9IL NILMLSVB*8BEPLA8KC42UIIUHSBKB+GIAZI3DJ/JAJZIR9KICT.XI/VB6TSYIJGDBGIA181:0TLOJJPACGKC2KRTI-8BEPL3DJ/LKQVBE2C*NIKYJIGK:H3J1DKVTQEDK8C+2TDSCNTCNJS6F3W.C\$USE\$2:*TIT3C7D8MS7LCTO3MMSSHT0\$U58PLY3 ZRA5PUF7MDN QKI7B\$WKL 6Q:S14GW4Q:LRERC6FPK1J*IUIH7S3J UQ2VQQ3ONV2CVR/TFFSQJ8KP.BENIQETGK6112U50-BW/IVK5"
        val credential = getTestJson(emptyName = true, emptyManufacturer = true)

        every {
            hint(JSONObject::class)
            mobileCoreWrapper.readEuropeanCredential(testQr.toByteArray())
        } returns credential

        with(useCase.get(testQr)) {
            with(events!!.first() as RemoteEventNegativeTest) {
                assertEquals(negativeTest!!.name, null)
                assertEquals(negativeTest!!.manufacturer, null)
            }
        }
    }

    @Test
    fun `an invalid qr should thow error`() {
        val invalidQr = "invalid"

        every {
            hint(JSONObject::class)
            mobileCoreWrapper.readEuropeanCredential(invalidQr.toByteArray())
        } returns JSONObject("{}")

        assertFails {
            useCase.get(invalidQr)
        }
    }

    private fun getVaccinationJson() = JSONObject(
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
                "                \"dn\": \"2\",\n" +
                "                \"sd\": \"2\",\n" +
                "                \"dt\": \"2021-07-07\",\n" +
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

    private fun getRecoveryJson() = JSONObject(
        "{\n" +
                "    \"credentialVersion\": 1,\n" +
                "    \"issuer\": \"NL\",\n" +
                "    \"issuedAt\": 1626247494,\n" +
                "    \"expirationTime\": 1629098542,\n" +
                "    \"dcc\": {\n" +
                "        \"ver\": \"1.3.0\",\n" +
                "        \"dob\": \"1960-01-01\",\n" +
                "        \"nam\": {\n" +
                "            \"fn\": \"De Bouwer\",\n" +
                "            \"fnt\": \"DE<BOUWER\",\n" +
                "            \"gn\": \"Bob\",\n" +
                "            \"gnt\": \"BOB\"\n" +
                "        },\n" +
                "        \"v\": null,\n" +
                "        \"t\": null,\n" +
                "        \"r\": [\n" +
                "            {\n" +
                "                \"tg\": \"840539006\",\n" +
                "                \"fr\": \"2021-06-30\",\n" +
                "                \"co\": \"NL\",\n" +
                "                \"is\": \"Ministry of Health Welfare and Sport\",\n" +
                "                \"df\": \"2021-06-30\",\n" +
                "                \"du\": \"2022-01-09\",\n" +
                "                \"ci\": \"URN:UCI:01:NL:ROIFOZLRYJF5TMC4RV3K42#A\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}"
    )

    private fun getTestJson(emptyName: Boolean = false, emptyManufacturer: Boolean = false) =
        JSONObject(
            "{\n" +
                    "    \"credentialVersion\": 1,\n" +
                    "    \"issuer\": \"NL\",\n" +
                    "    \"issuedAt\": 1626247494,\n" +
                    "    \"expirationTime\": 1629098683,\n" +
                    "    \"dcc\": {\n" +
                    "        \"ver\": \"1.3.0\",\n" +
                    "        \"dob\": \"1960-01-01\",\n" +
                    "        \"nam\": {\n" +
                    "            \"fn\": \"De Bouwer\",\n" +
                    "            \"fnt\": \"DE<BOUWER\",\n" +
                    "            \"gn\": \"Bob\",\n" +
                    "            \"gnt\": \"BOB\"\n" +
                    "        },\n" +
                    "        \"v\": null,\n" +
                    "        \"t\": [\n" +
                    "            {\n" +
                    "                \"tg\": \"840539006\",\n" +
                    "                \"tt\": \"LP6464-4\",\n" +
                    "                \"nm\": \"${if (!emptyName) "name" else ""}\",\n" +
                    "                \"ma\": \"${if (!emptyManufacturer) "manufacturer" else ""}\",\n" +
                    "                \"sc\": \"2021-07-13T16:31:35+00:00\",\n" +
                    "                \"dr\": \"\",\n" +
                    "                \"tr\": \"260415000\",\n" +
                    "                \"tc\": \"Facility approved by the State of The Netherlands\",\n" +
                    "                \"co\": \"NL\",\n" +
                    "                \"is\": \"Ministry of Health Welfare and Sport\",\n" +
                    "                \"ci\": \"URN:UCI:01:NL:B3PEER674NFX3C3VA3XD42#P\"\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"r\": null\n" +
                    "    }\n" +
                    "}"
        )
}