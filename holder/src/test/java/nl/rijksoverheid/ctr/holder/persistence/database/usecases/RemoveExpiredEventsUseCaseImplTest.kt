package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.fakeCachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class RemoveExpiredEventsUseCaseImplTest {

    private val cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
        appConfig = HolderConfig.default(
            vaccinationEventValidityDays = 10,
            testEventValidityHours = TimeUnit.DAYS.toHours(20).toInt(),
            recoveryEventValidityDays = 30
        )
    )

    private val eventGroupDao = mockk<EventGroupDao>(relaxed = true)
    private val holderDatabase = mockk<HolderDatabase>(relaxed = true).apply {
        coEvery { eventGroupDao() } returns eventGroupDao
    }
    private val firstJanuaryInstant = Instant.parse("2021-01-01T00:00:00.00Z")
    private val firstJanuaryClock = Clock.fixed(firstJanuaryInstant, ZoneId.of("UTC"))
    private val firstJanuaryDate = OffsetDateTime.ofInstant(firstJanuaryInstant, ZoneId.of("UTC"))

    private val usecase = RemoveExpiredEventsUseCaseImpl(
        clock = firstJanuaryClock,
        cachedAppConfigUseCase = cachedAppConfigUseCase,
        holderDatabase = holderDatabase
    )

    @Test
    fun `Vaccination event is cleared from database when expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Vaccination,
            maxIssuedAt = firstJanuaryDate.minusDays(10),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify { eventGroupDao.delete(eventGroup) }
    }

    @Test
    fun `Vaccination event is not cleared from database when not expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Vaccination,
            maxIssuedAt = firstJanuaryDate.minusDays(9),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify(exactly = 0) { eventGroupDao.delete(eventGroup) }
    }

    @Test
    fun `Test event is cleared from database when expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Test,
            maxIssuedAt = firstJanuaryDate.minusDays(20),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify { eventGroupDao.delete(eventGroup) }
    }

    @Test
    fun `Test event is not cleared from database when not expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Test,
            maxIssuedAt = firstJanuaryDate.minusDays(19),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify(exactly = 0) { eventGroupDao.delete(eventGroup) }
    }

    @Test
    fun `Recovery event is cleared from database when expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Recovery,
            maxIssuedAt = firstJanuaryDate.minusDays(30),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify { eventGroupDao.delete(eventGroup) }
    }

    @Test
    fun `Recovery event is not cleared from database when not expired`() = runBlocking {
        val eventGroup = EventGroupEntity(
            walletId = 1,
            providerIdentifier = "",
            type = OriginType.Recovery,
            maxIssuedAt = firstJanuaryDate.minusDays(29),
            jsonData = "".toByteArray()
        )

        usecase.execute(listOf(eventGroup))
        coVerify(exactly = 0) { eventGroupDao.delete(eventGroup) }
    }
}