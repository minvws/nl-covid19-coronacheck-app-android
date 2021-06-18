package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class GreenCardsUseCaseImplTest {
    
    private val greenCardDao = mockk<GreenCardDao>(relaxed = true)
    private val holderDatabase = mockk< HolderDatabase>(relaxed = true).apply { 
        coEvery { greenCardDao() } returns greenCardDao
    }
    private val appConfig = mockk<AppConfig>(relaxed = true)
    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true).apply { 
        coEvery { getCachedAppConfig() } returns appConfig
    }

    private val firstJanuaryClock = Clock.fixed(Instant.parse("2021-01-05T00:00:00.00Z"), ZoneId.of("UTC"))
    private val greenCardUseCase = GreenCardsUseCaseImpl(holderDatabase, cachedAppConfigUseCase, firstJanuaryClock)
    
    private fun greenCard(
        originEntities: List<OriginEntity>? = null,
        credentials: List<CredentialEntity>? = null) = mockk<GreenCard>(relaxed = true).apply {
        if (originEntities != null) {
            coEvery { origins } returns originEntities
        }

        if (credentials != null) {
            coEvery { credentialEntities } returns credentials
        }
    }

    private fun credentialEntity(credentialVersion: Int = 2, expireDateTime: String = "2021-01-07T07:00:00.00Z") = CredentialEntity(1L, 1L, "".toByteArray(), credentialVersion, OffsetDateTime.now(), OffsetDateTime.ofInstant(
        Instant.parse(expireDateTime),
        ZoneId.of("UTC")
    ))

    private fun validOriginEntity() = OriginEntity(1, 1L, OriginType.Test, OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now())

    private fun validGreenCard() = greenCard(
        originEntities = listOf(validOriginEntity()),
        credentials = listOf(credentialEntity(), credentialEntity())
    )

    private fun expiringGreenCard() = greenCard(
        originEntities = listOf(validOriginEntity()),
        credentials = listOf(credentialEntity(expireDateTime = "2021-01-01T07:00:00.00Z"), credentialEntity(expireDateTime = "2021-01-01T07:00:00.00Z"))
    )

    private fun unsupportedGreenCard() = greenCard(
        originEntities = listOf(validOriginEntity()),
        credentials = listOf(credentialEntity(credentialVersion = 1), credentialEntity())
    )

    @Test
    fun `given two green cards with some credentials, when both green cards do not expire, then return null (no refresh)`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), validGreenCard())

        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertNull(expiringCardOriginType)
    }

    @Test
    fun `given two green cards with some credentials, when a green card expires and the other green card does not expire, then return an expiring origin type to refresh`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), expiringGreenCard())

        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertEquals(OriginType.Test, expiringCardOriginType)
    }

    @Test
    fun `given a green card with some credentials, when all credentials expire, then return the expiring origin type to refresh`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(expiringGreenCard(), expiringGreenCard())

        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertEquals(OriginType.Test, expiringCardOriginType)
    }

    fun `given a green card with some credentials, when the last credential does not expire, then return null (no refresh)`() = runBlocking {

    }

    fun `given a green card with no credentials, then return an origin type to refresh`() = runBlocking {
        
    }

    @Test
    fun `given a green card with some credentials, when a credential version is not supported anymore, then return an origin type to refresh`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), unsupportedGreenCard())

        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertEquals(OriginType.Test, expiringCardOriginType)
    }

    fun `given a green card with some credentials, when all credential versions are invalid, then return an origin type to refresh`() = runBlocking {

    }

    @Test
    fun `given a green card with some credentials, when all credential versions are still supported, then return null (no refresh)`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), validGreenCard())

        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertNull(expiringCardOriginType)
    }

    @Test
    fun `given a green card with some credentials, when the green card has no origin, then return null (no refresh)`() = runBlocking {
        coEvery { appConfig.minimumCredentialVersion } returns 2
        coEvery { greenCardDao.getAll() } returns listOf(greenCard(originEntities = emptyList()), greenCard(originEntities = emptyList()))
        
        val expiringCardOriginType = greenCardUseCase.expiringCardOriginType()

        assertNull(expiringCardOriginType)
    }
}