package nl.rijksoverheid.ctr.dashboard.util

import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtilImpl
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtilImpl
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
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
class CredentialUtilImplTest : AutoCloseKoinTest() {

    private fun credentialIdentity(expirationTime: OffsetDateTime) = CredentialEntity(
        id = 1,
        greenCardId = 1L,
        data = "".toByteArray(),
        credentialVersion = 2,
        validFrom = OffsetDateTime.now(),
        expirationTime = expirationTime
    )

    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)

    private val countryUtil: CountryUtil = mockk(relaxed = true)

    @Test
    fun `domestic getActiveCredential returns active credential with highest expiration time`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))

        val credential1 = CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(100), ZoneOffset.UTC)
        )

        val credential2 = CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(20), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1000), ZoneOffset.UTC)
        )

        val activeCredential = credentialUtil.getActiveCredential(
            greenCardType = GreenCardType.Eu,
            entities = listOf(credential1, credential2)
        )

        assertEquals(credential2, activeCredential)
    }

    @Test
    fun `domestic getActiveCredential returns no active credential if not in window`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))

        val credential1 = CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(49), ZoneOffset.UTC)
        )

        val credential2 = CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(100), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1000), ZoneOffset.UTC)
        )

        val activeCredential = credentialUtil.getActiveCredential(
            greenCardType = GreenCardType.Eu,
            entities = listOf(credential1, credential2)
        )

        assertNull(null, activeCredential)
    }

    @Test
    fun `international getActiveCredential ignores validFrom`() {
        val clock = Clock.fixed(Instant.parse("2022-01-02T09:00:00.00Z"), ZoneId.of("UTC"))
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))

        val credential = CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.ofInstant(Instant.parse("2022-01-01T09:00:00.00Z"), ZoneOffset.UTC),
            expirationTime = OffsetDateTime.ofInstant(Instant.parse("2022-01-03T09:00:00.00Z"), ZoneOffset.UTC)
        )

        val activeCredential = credentialUtil.getActiveCredential(
            greenCardType = GreenCardType.Eu,
            entities = listOf(credential)
        )

        assertEquals(credential, activeCredential)
    }

    @Test
    fun `given a credential expiring before the renewal date, when isExpiring, then it returns true`() {
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-03T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))
        assertTrue(credentialUtil.isExpiring(5L, credentialEntity))
    }

    @Test
    fun `given a credential expiring after the renewal date, when isExpiring, then it returns false`() {
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-06T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))
        assertFalse(credentialUtil.isExpiring(5L, credentialEntity))
    }

    @Test
    fun `given a credential expiring exactly on the renewal date, when isExpiring, then it returns true`() {
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk(), countryUtil, mockk(relaxed = true))
        assertTrue(credentialUtil.isExpiring(5L, credentialEntity))
    }

    @Test
    fun `Vaccination should be hidden if it's older than relevancy days and of lower dose than maximum`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1609498800), ZoneId.of("UTC")) // 2021-01-01
        val appConfigUseCase: HolderCachedAppConfigUseCase = mockk {
            every { getCachedAppConfig() } returns HolderConfig.default(internationalQRRelevancyDays = 28)
        }
        val util = CredentialUtilImpl(clock, mockk(), appConfigUseCase, countryUtil, mockk(relaxed = true))

        val oneVaccination = listOf(getVaccinationJson("2020-12-01", dose = 1, ofTotalDoses = 2))
        val notHiddenBecauseOfDate =
            listOf(getVaccinationJson("2021-01-01", dose = 1, ofTotalDoses = 2))
        val notHiddenBecauseOfDose =
            listOf(getVaccinationJson("2020-12-01", dose = 2, ofTotalDoses = 2))

        assertFalse(util.vaccinationShouldBeHidden(oneVaccination, 0))
        assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDate, 0))
        assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDose, 0))
    }

    @Test
    fun `Hidden vaccination should not be hidden when there is a completed vaccination which is not relevant yet and the upcoming dose is 1 above the hidden one`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1609498800), ZoneId.of("UTC")) // 2021-01-01
        val appConfigUseCase: HolderCachedAppConfigUseCase = mockk {
            every { getCachedAppConfig() } returns HolderConfig.default(internationalQRRelevancyDays = 28)
        }
        val util = CredentialUtilImpl(clock, mockk(), appConfigUseCase, countryUtil, mockk(relaxed = true))

        val notHidden = listOf(
            getVaccinationJson("2020-12-01", dose = 1, ofTotalDoses = 2),
            getVaccinationJson("2020-12-25", dose = 2, ofTotalDoses = 2)
        )
        val hidden = listOf(
            getVaccinationJson("2020-12-01", dose = 1, ofTotalDoses = 2),
            getVaccinationJson("2020-12-02", dose = 2, ofTotalDoses = 2)
        )
        val hidden2 = listOf(
            getVaccinationJson("2020-12-01", dose = 1, ofTotalDoses = 3),
            getVaccinationJson("2020-12-02", dose = 2, ofTotalDoses = 3),
            getVaccinationJson("2020-12-25", dose = 3, ofTotalDoses = 3)
        )

        assertFalse(util.vaccinationShouldBeHidden(notHidden, 0))
        assertTrue(util.vaccinationShouldBeHidden(hidden2, 0))
        assertTrue(util.vaccinationShouldBeHidden(hidden, 0))
    }

    @Test
    fun `dutch dcc returns correct dosis string`() {
        every { mobileCoreWrapper.readEuropeanCredential(any()) } returns getVaccinationJson()
        val credentialUtil = CredentialUtilImpl(Clock.systemUTC(), mobileCoreWrapper, mockk(), CountryUtilImpl(), mockk(relaxed = true))

        val dosisString = credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(listOf(credentialIdentity(
            OffsetDateTime.ofInstant(
                Instant.parse("2022-10-03T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )), "NL") { dn, sd, country ->
            assertTrue(country.isEmpty())
            "$dn/$sd"
        }

        assertEquals("2/2", dosisString)
    }

    @Test
    fun `eu dcc returns correct dosis string`() {
        every { mobileCoreWrapper.readEuropeanCredential(any()) } returns getVaccinationJson(countryCode = "IT")
        val credentialUtil = CredentialUtilImpl(Clock.systemUTC(), mobileCoreWrapper, mockk(), CountryUtilImpl(), mockk(relaxed = true))

        val dosisString = credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(listOf(credentialIdentity(
            OffsetDateTime.ofInstant(
                Instant.parse("2022-10-03T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )), "NL") { dn, sd, country ->
            "$dn/$sd$country"
        }

        assertEquals("2/2 (Italy)", dosisString)
    }

    private fun getVaccinationJson(date: String = "2020-12-01", dose: Int = 2, ofTotalDoses: Int = 2, countryCode: String = "NL") =
        JSONObject(
            "{\n" +
                    "    \"credentialVersion\": 1,\n" +
                    "    \"issuer\": \"$countryCode\",\n" +
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
                    "                \"dn\": $dose,\n" +
                    "                \"sd\": $ofTotalDoses,\n" +
                    "                \"dt\": \"$date\",\n" +
                    "                \"co\": \"$countryCode\",\n" +
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
