package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.junit.Assert.*
import org.junit.Test
import java.time.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CredentialUtilImplTest {

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
}