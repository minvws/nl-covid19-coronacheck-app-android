package nl.rijksoverheid.ctr.persistence.database.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
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
class RemoveExpiredEventsUseCaseImplTest {

    private val eventGroupDao = mockk<EventGroupDao>(relaxed = true)
    private val holderDatabase = mockk<HolderDatabase>(relaxed = true).apply {
        coEvery { eventGroupDao() } returns eventGroupDao
    }

    @Test
    fun `Event should not be removed if expire date is null`() = runBlocking {
        val eventGroup = getEventGroupEntity(null)
        val usecase = RemoveExpiredEventsUseCaseImpl(
            clock = Clock.fixed(Instant.parse("2021-01-05T00:00:00.00Z"), ZoneId.of("UTC")),
            holderDatabase = holderDatabase
        )
        usecase.execute(
            events = listOf(eventGroup)
        )
        coVerify(exactly = 0) { eventGroupDao.delete(any()) }
    }

    @Test
    fun `Event should not be removed if not yet expired`() = runBlocking {
        val eventGroup = getEventGroupEntity(OffsetDateTime.ofInstant(Instant.parse("2021-01-10T00:00:00.00Z"), ZoneId.of("UTC")))
        val usecase = RemoveExpiredEventsUseCaseImpl(
            clock = Clock.fixed(Instant.parse("2021-01-05T00:00:00.00Z"), ZoneId.of("UTC")),
            holderDatabase = holderDatabase
        )
        usecase.execute(
            events = listOf(eventGroup)
        )
        coVerify(exactly = 0) { eventGroupDao.delete(any()) }
    }

    @Test
    fun `Event should be removed if expired`() = runBlocking {
        val eventGroup = getEventGroupEntity(OffsetDateTime.ofInstant(Instant.parse("2021-01-02T00:00:00.00Z"), ZoneId.of("UTC")))
        val usecase = RemoveExpiredEventsUseCaseImpl(
            clock = Clock.fixed(Instant.parse("2021-01-05T00:00:00.00Z"), ZoneId.of("UTC")),
            holderDatabase = holderDatabase
        )
        usecase.execute(
            events = listOf(eventGroup)
        )
        coVerify(exactly = 1) { eventGroupDao.delete(eventGroup) }
    }

    private fun getEventGroupEntity(expiryDate: OffsetDateTime?) = EventGroupEntity(
        id = 1,
        walletId = 1,
        providerIdentifier = "",
        type = OriginType.Test,
        scope = "",
        expiryDate = expiryDate,
        jsonData = "".toByteArray()
    )
}