/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtilImpl
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginHintEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import org.junit.Assert.assertEquals
import org.junit.Test

class GreenCardUtilImplTest {

    private val holderDatabase = mockk<HolderDatabase>(relaxed = true)
    private val credentialUtil = mockk<CredentialUtil>(relaxed = true)

    @Test
    fun `getAllGreenCards returns green cards with origins`() = runBlocking {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))

        val greenCardWithOrigin = getGreenCard(origins = listOf(fakeOriginEntity()))
        val greenCardWithoutOrigin = getGreenCard(origins = listOf())

        coEvery { holderDatabase.greenCardDao().getAll() } answers { listOf(greenCardWithOrigin, greenCardWithoutOrigin) }

        val util = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())
        val allGreenCards = util.getAllGreenCards()

        assertEquals(1, allGreenCards.size)
        assertEquals(greenCardWithOrigin, allGreenCards.first())
    }

    @Test
    fun `hasOrigin returns true if green cards contain origin`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val util = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = fakeGreenCard(originType = OriginType.Vaccination)

        val hasOrigin = util.hasOrigin(
            greenCards = listOf(greenCard),
            originType = OriginType.Vaccination
        )

        assertTrue(hasOrigin)
    }

    @Test
    fun `hasOrigin returns false if green cards does not contain origin`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val util = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = fakeGreenCard(originType = OriginType.Recovery)

        val hasOrigin = util.hasOrigin(
            greenCards = listOf(greenCard),
            originType = OriginType.Vaccination
        )

        assertFalse(hasOrigin)
    }

    @Test
    fun `getExpireDate returns expired date of origin furthest away`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).plusHours(1),
                    validFrom = OffsetDateTime.now()
                ),
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).plusHours(2),
                    validFrom = OffsetDateTime.now()
                )
            ),
            credentialEntities = listOf()
        )

        assertEquals(OffsetDateTime.now(clock).plusHours(2), greenCardUtil.getExpireDate(greenCard))
    }

    @Test
    fun `getExpireDate returns expired date of chosen origin type`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).plusHours(1),
                    validFrom = OffsetDateTime.now()
                ),
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).plusHours(2),
                    validFrom = OffsetDateTime.now()
                )
            ),
            credentialEntities = listOf()
        )

        assertEquals(OffsetDateTime.now(clock).plusHours(1), greenCardUtil.getExpireDate(greenCard, OriginType.Test))
    }

    @Test
    fun `isExpired returns true if expire date in past`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).minusHours(10),
                    validFrom = OffsetDateTime.now()
                ),
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).minusHours(5),
                    validFrom = OffsetDateTime.now()
                )
            ),
            credentialEntities = listOf()
        )

        assertEquals(true, greenCardUtil.isExpired(greenCard))
    }

    @Test
    fun `isExpired returns false if expire date in future`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).minusHours(10),
                    validFrom = OffsetDateTime.now()
                ),
                OriginEntity(
                    id = 0,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(clock).plusHours(10),
                    validFrom = OffsetDateTime.now()
                )
            ),
            credentialEntities = listOf()
        )

        assertEquals(false, greenCardUtil.isExpired(greenCard))
    }

    @Test
    fun `getErrorCorrectionLevel returns correct levels for eu green card type`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        assertEquals(ErrorCorrectionLevel.Q, greenCardUtil.getErrorCorrectionLevel(GreenCardType.Eu))
    }

    @Test
    fun `hasNoActiveCredentials returns true when there are no active credentials for this green card`() {
        every { credentialUtil.getActiveCredential(any(), any()) } answers { null }

        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(),
            credentialEntities = listOf()
        )

        assertTrue { greenCardUtil.hasNoActiveCredentials(greenCard) }
    }

    @Test
    fun `hasNoActiveCredentials returns false when there are active credentials for this green card`() {
        every { credentialUtil.getActiveCredential(any(), any()) } answers {
            CredentialEntity(
                id = 1,
                greenCardId = 1L,
                data = "".toByteArray(),
                credentialVersion = 1,
                validFrom = OffsetDateTime.now(),
                expirationTime = OffsetDateTime.now()
            )
        }

        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(holderDatabase, clock, credentialUtil, mockk())

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(),
            credentialEntities = listOf()
        )

        assertFalse { greenCardUtil.hasNoActiveCredentials(greenCard) }
    }

    @Test
    fun `isEventFromDcc returns true if green card origin has event_from_dcc hint`() {
        val greenCard = getGreenCard(
            type = GreenCardType.Eu,
            origins = listOf(
                getOriginEntity(id = 1),
                getOriginEntity(id = 2)
            )
        )

        val hints = listOf(
            OriginHintEntity(id = 1, originId = 1, hint = "event_from_dcc")
        )

        val greenCardUtil = GreenCardUtilImpl(holderDatabase, mockk(), credentialUtil, mockk())

        assertTrue(greenCardUtil.isEventFromDcc(greenCard, hints))
    }

    @Test
    fun `isEventFromDcc returns false if green card origin does not have event_from_dcc hint`() {
        val greenCard1 = getGreenCard(
            type = GreenCardType.Eu,
            origins = listOf(
                getOriginEntity(id = 1),
                getOriginEntity(id = 2)
            )
        )
        val greenCard2 = getGreenCard(
            type = GreenCardType.Eu,
            origins = listOf(
                getOriginEntity(id = 5),
                getOriginEntity(id = 6)
            )
        )

        val hints = listOf(
            OriginHintEntity(id = 1, originId = 3, hint = "event_from_dcc")
        )

        val greenCardUtil = GreenCardUtilImpl(holderDatabase, mockk(), credentialUtil, mockk())

        assertFalse(greenCardUtil.isEventFromDcc(greenCard1, hints))
        assertFalse(greenCardUtil.isEventFromDcc(greenCard2, hints))
    }

    private fun getOriginEntity(id: Int) = OriginEntity(
        id = id,
        greenCardId = 1,
        expirationTime = OffsetDateTime.now(),
        type = OriginType.Vaccination,
        eventTime = OffsetDateTime.now(),
        validFrom = OffsetDateTime.now()
    )

    private fun getGreenCard(
        type: GreenCardType = GreenCardType.Eu,
        origins: List<OriginEntity> = listOf()
    ) = GreenCard(
        greenCardEntity = GreenCardEntity(
            id = 0,
            walletId = 0,
            type = type
        ),
        origins = origins,
        credentialEntities = listOf(
            CredentialEntity(
                id = 0,
                greenCardId = 0,
                data = "".toByteArray(),
                credentialVersion = 0,
                validFrom = OffsetDateTime.now(),
                expirationTime = OffsetDateTime.now(),
                category = null
            )
        )
    )
}
