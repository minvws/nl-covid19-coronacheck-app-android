package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.*

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
        expirationTime = expirationTime,
    )

    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)

    @Test
    fun `getActiveCredential returns active credential with highest expiration time`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk())

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
            entities = listOf(credential1, credential2)
        )

        assertEquals(credential2, activeCredential)
    }

    @Test
    fun `getActiveCredential returns no active credential if not in window`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk())

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
            entities = listOf(credential1, credential2)
        )

        assertNull(null, activeCredential)
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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk())
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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk())
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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper, mockk())
        assertTrue(credentialUtil.isExpiring(5L, credentialEntity))
    }

    @Test
    fun `Vaccination should be hidden if it's older than relevancy days and of lower dose than maximum`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1609498800), ZoneId.of("UTC")) // 2021-01-01
        val appConfigUseCase: CachedAppConfigUseCase = mockk {
            every { getCachedAppConfig() } returns HolderConfig.default(internationalQRRelevancyDays = 28)
        }
        val util = CredentialUtilImpl(clock, mockk(), appConfigUseCase)

        val hidden = listOf(getVaccinationJson("2020-12-01", dose = "1", ofTotalDoses = "2"))
        val notHiddenBecauseOfDate =
            listOf(getVaccinationJson("2021-01-01", dose = "1", ofTotalDoses = "2"))
        val notHiddenBecauseOfDose =
            listOf(getVaccinationJson("2020-12-01", dose = "2", ofTotalDoses = "2"))

        kotlin.test.assertTrue(util.vaccinationShouldBeHidden(hidden, 0))
        kotlin.test.assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDate, 0))
        kotlin.test.assertFalse(util.vaccinationShouldBeHidden(notHiddenBecauseOfDose, 0))
    }

    @Test
    fun `Hidden vaccination should not be hidden when there is a completed vaccination which is not relevant yet and the upcoming dose is 1 above the hidden one`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(1609498800), ZoneId.of("UTC")) // 2021-01-01
        val appConfigUseCase: CachedAppConfigUseCase = mockk {
            every { getCachedAppConfig() } returns HolderConfig.default(internationalQRRelevancyDays = 28)
        }
        val util = CredentialUtilImpl(clock, mockk(), appConfigUseCase)

        val notHidden = listOf(
            getVaccinationJson("2020-12-01", dose = "1", ofTotalDoses = "2"),
            getVaccinationJson("2020-12-25", dose = "2", ofTotalDoses = "2")
        )
        val hidden = listOf(
            getVaccinationJson("2020-12-01", dose = "1", ofTotalDoses = "2"),
            getVaccinationJson("2020-12-02", dose = "2", ofTotalDoses = "2")
        )
        val hidden2 = listOf(
            getVaccinationJson("2020-12-01", dose = "1", ofTotalDoses = "3"),
            getVaccinationJson("2020-12-02", dose = "2", ofTotalDoses = "3"),
            getVaccinationJson("2020-12-25", dose = "3", ofTotalDoses = "3")
        )

        assertFalse(util.vaccinationShouldBeHidden(notHidden, 0))
        assertTrue(util.vaccinationShouldBeHidden(hidden2, 0))
        assertTrue(util.vaccinationShouldBeHidden(hidden, 0))
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