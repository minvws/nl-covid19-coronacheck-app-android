package nl.rijksoverheid.ctr.holder.persistence.database.entities

import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CredentialEntityTest {

    private val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

    private fun credentialIdentity(expirationTime: OffsetDateTime) = CredentialEntity(
        id = 1,
        greenCardId = 1L,
        data = "".toByteArray(),
        credentialVersion = 2,
        validFrom = OffsetDateTime.now(),
        expirationTime = expirationTime,
    )

    @Test
    fun `given a credential expiring before the renewal date, when isExpiring, then it returns true`() {
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-03T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val expiring = credentialEntity.isExpiring(5L, clock)

        assertTrue(expiring)
    }

    @Test
    fun `given a credential expiring after the renewal date, when isExpiring, then it returns false`() {
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-06T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val expiring = credentialEntity.isExpiring(5L, clock)

        assertFalse(expiring)
    }

    @Test
    fun `given a credential expiring exactly on the renewal date, when isExpiring, then it returns true`() {
        val credentialEntity = credentialIdentity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )

        val expiring = credentialEntity.isExpiring(5L, clock)

        assertTrue(expiring)
    }
}