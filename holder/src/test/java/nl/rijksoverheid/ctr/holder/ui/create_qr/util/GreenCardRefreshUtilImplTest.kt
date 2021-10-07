package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
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
class GreenCardRefreshUtilImplTest {
    
    private val greenCardDao = mockk<GreenCardDao>(relaxed = true)
    private val holderDatabase = mockk< HolderDatabase>(relaxed = true).apply { 
        coEvery { greenCardDao() } returns greenCardDao
    }
    private val appConfig = mockk<HolderConfig>(relaxed = true).apply {
        coEvery { credentialRenewalDays } returns 5
    }
    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true).apply {
        coEvery { getCachedAppConfig() } returns appConfig
    }

    private val greenCardUtil: GreenCardUtil = mockk(relaxed = true)

    private val firstJanuaryClock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)
    private val credentialUtil = CredentialUtilImpl(firstJanuaryClock, mobileCoreWrapper, mockk())

    private val greenCardRefreshUtil = GreenCardRefreshUtilImpl(holderDatabase, cachedAppConfigUseCase, greenCardUtil, firstJanuaryClock, credentialUtil)
    
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

    private fun validGreenCard(expireDateTime1: String = "2021-01-06T07:00:00.00Z",
                               expireDateTime2: String = "2021-01-06T07:00:00.00Z") = greenCard(
        originEntities = listOf(validOriginEntity()),
        credentials = listOf(credentialEntity(expireDateTime = expireDateTime1),
            credentialEntity(expireDateTime = expireDateTime2))
    )

    private fun expiringGreenCard(expireDateTime1: String = "2021-01-02T07:00:00.00Z",
                                  expireDateTime2: String = "2021-01-03T07:00:00.00Z") = greenCard(
        originEntities = listOf(validOriginEntity()),
        credentials = listOf(credentialEntity(expireDateTime = expireDateTime1),
            credentialEntity(expireDateTime = expireDateTime2))
    )

    @Test
    fun `given two green cards with some credentials, when both green cards do not expire, then return false (no refresh)`() = runBlocking {
        
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), validGreenCard())

        val expiring = greenCardRefreshUtil.shouldRefresh()

        assertFalse(expiring)
    }

    @Test
    fun `given two green cards with some credentials, when a green card expires and the other green card does not expire, then return true to refresh`() = runBlocking {
        
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard(), expiringGreenCard())

        val expiring = greenCardRefreshUtil.shouldRefresh()

        assertTrue(expiring)
    }

    @Test
    fun `given a green card with some credentials, when all credentials expire, then return true to refresh`() = runBlocking {
        
        coEvery { greenCardDao.getAll() } returns listOf(expiringGreenCard(), expiringGreenCard())

        val expiring = greenCardRefreshUtil.shouldRefresh()

        assertTrue(expiring)
    }

    @Test
    fun `given a green card with some credentials, when all credential versions are still supported, then return false (no refresh)`() = runBlocking {
        
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard())

        val expiring = greenCardRefreshUtil.shouldRefresh()

        assertFalse(expiring)
    }

    @Test
    fun `given a green with credential expiring in 7 days after now, when checking the last expiring one, then it returns it with refreshInDays set to 2`() = runBlocking {
        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard("2021-01-08T07:00:00.00Z"))

        val credentialsExpireInDays = greenCardRefreshUtil.credentialsExpireInDays()

        assertEquals(2, credentialsExpireInDays)
    }

    @Test
    fun `given a green with credential expiring 6 days after now, when checking the last expiring one, then it returns it with refreshInDays set to 1`() = runBlocking {

        coEvery { greenCardDao.getAll() } returns listOf(validGreenCard())

        val credentialsExpireInDays = greenCardRefreshUtil.credentialsExpireInDays()

        assertEquals(1, credentialsExpireInDays)
    }

    @Test
    fun `given a green with credential expiring today, when checking the last expiring one, then it returns it with refreshInDays set to 1`() = runBlocking {
        coEvery { greenCardDao.getAll() } returns listOf(expiringGreenCard())

        val credentialsExpireInDays = greenCardRefreshUtil.credentialsExpireInDays()

        assertEquals(1, credentialsExpireInDays)
    }

    @Test
    fun `given no green cards, when checking the last expiring one, then it returns None (so no refresh is scheduled)`() = runBlocking {
        coEvery { greenCardDao.getAll() } returns listOf()

        val credentialsExpireInDays = greenCardRefreshUtil.credentialsExpireInDays()

        assertEquals(0, credentialsExpireInDays)
    }
}