package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtilImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtilImpl
import org.junit.Assert
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GreenCardUtilImplTest {

    private val credentialUtil = mockk<CredentialUtil>(relaxed = true)

    @Test
    fun `getExpireDate returns expired date of origin furthest away`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
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
                    type = OriginType.Test,
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
    fun `isExpired returns true if expire date in past`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
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
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
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
    fun `getErrorCorrectionLevel returns correct levels for domestic green card type`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        assertEquals(ErrorCorrectionLevel.M, greenCardUtil.getErrorCorrectionLevel(GreenCardType.Domestic))
    }

    @Test
    fun `getErrorCorrectionLevel returns correct levels for eu green card type`() {
        val clock = Clock.fixed(Instant.ofEpochSecond(50), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        assertEquals(ErrorCorrectionLevel.Q, greenCardUtil.getErrorCorrectionLevel(GreenCardType.Eu))
    }

    @Test
    fun `hasNoActiveCredentials returns true when there are no active credentials for this green card`() {
        every { credentialUtil.getActiveCredential(any()) } answers { null }

        val clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
            ),
            origins = listOf(),
            credentialEntities = listOf()
        )

        assertTrue { greenCardUtil.hasNoActiveCredentials(greenCard) }
    }

    @Test
    fun `hasNoActiveCredentials returns false when there are active credentials for this green card`() {
        every { credentialUtil.getActiveCredential(any()) } answers {
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
        val greenCardUtil = GreenCardUtilImpl(clock, credentialUtil)

        val greenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
            ),
            origins = listOf(),
            credentialEntities = listOf()
        )

        assertFalse { greenCardUtil.hasNoActiveCredentials(greenCard) }
    }

}