package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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
class CredentialUtilImplTest: AutoCloseKoinTest() {

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
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper)

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
        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper)

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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper)
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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper)
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

        val credentialUtil = CredentialUtilImpl(clock, mobileCoreWrapper)
        assertTrue(credentialUtil.isExpiring(5L, credentialEntity))
    }

    @Test
    fun `european credentials of 1 dose vaccine`() {
        val readEuropeanCredentialVaccination = "{\"credentialVersion\":1,\"issuer\":\"NL\",\"issuedAt\":1627294308,\"expirationTime\":1629717843,\"dcc\":{\"ver\":\"1.3.0\",\"dob\":\"1960-01-01\",\"nam\":{\"fn\":\"Bouwer\",\"fnt\":\"BOUWER\",\"gn\":\"Bob\",\"gnt\":\"BOB\"},\"v\":[{\"tg\":\"840539006\",\"vp\":\"1119349007\",\"mp\":\"EU\\/1\\/20\\/1528\",\"ma\":\"ORG-100030215\",\"dn\":1,\"sd\":1,\"dt\":\"2021-07-18\",\"co\":\"NL\",\"is\":\"Ministry of Health Welfare and Sport\",\"ci\":\"URN:UCI:01:NL:FE6BOX7GLBBZTH6K5OFO42#1\"}],\"t\":null,\"r\":null}}"
        every { mobileCoreWrapper.readEuropeanCredential(any()) } returns JSONObject(readEuropeanCredentialVaccination)

        val credentialUtil = CredentialUtilImpl(Clock.systemUTC(), mobileCoreWrapper)
        val dosesString = credentialUtil.getVaccinationDosesForEuropeanCredentials(listOf(mockk(relaxed = true))) { doseNumber, sumDoses ->
            "Dose $doseNumber of $sumDoses"
        }

        assertEquals("Dose 1 of 1", dosesString)
    }

    @Test
    fun `test type of NAAT test`() {
        val readEuropeanCredentialTest = "{\"credentialVersion\":1,\"issuer\":\"NL\",\"issuedAt\":1627401558,\"expirationTime\":1629820758,\"dcc\":{\"ver\":\"1.3.0\",\"dob\":\"1982-08-01\",\"nam\":{\"fn\":\"Epitheto\",\"fnt\":\"EPITHETO\",\"gn\":\"Onoma\",\"gnt\":\"ONOMA\"},\"v\":null,\"t\":[{\"tg\":\"840539006\",\"tt\":\"LP217198-3\",\"nm\":\"\",\"ma\":\"\",\"sc\":\"2021-07-27T16:57:00+02:00\",\"dr\":\"\",\"tr\":\"260415000\",\"tc\":\"Facility approved by the State of The Netherlands\",\"co\":\"NL\",\"is\":\"Ministry of Health Welfare and Sport\",\"ci\":\"URN:UCI:01:NL:RK3RLAOFRBAN5GXCL2KY42#H\"}],\"r\":null}}"
        every { mobileCoreWrapper.readEuropeanCredential(any()) } returns JSONObject(readEuropeanCredentialTest)

        val credentialUtil = CredentialUtilImpl(Clock.systemUTC(), mobileCoreWrapper)
        val testType = credentialUtil.getTestTypeForEuropeanCredentials(listOf(mockk(relaxed = true)))

        assertEquals("RAT", testType)
    }
}