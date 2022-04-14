/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.saved_events.usecases

import kotlinx.coroutines.*
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.saved_events.usecases.GetSavedEventsUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class GetSavedEventsUseCaseImplTest: AutoCloseKoinTest() {

    private val holderDatabase: HolderDatabase by inject()
    private val usecase: GetSavedEventsUseCase by inject()

    private lateinit var db: HolderDatabase

    @After
    fun setup() = runBlocking {
        withContext(Dispatchers.IO) {
            holderDatabase.clearAllTables()
        }
    }

    @Test
    fun `No saved events are returned when there are no stored events`() = runBlocking {
        val savedEvents = usecase.getSavedEvents()
        assertTrue(savedEvents.isEmpty())
    }

    @Test
    fun `Saved events are returned when there is a stored event`() = runBlocking {
        val walletEntity = WalletEntity(
            id = 1,
            label = "main"
        )

        val eventGroupEntity = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "GGD",
            type = OriginType.Vaccination,
            scope = null,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "{\"signature\":\"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\",\"payload\":\"eyJwcm90b2NvbFZlcnNpb24iOiIzLjAiLCJwcm92aWRlcklkZW50aWZpZXIiOiJaWloiLCJzdGF0dXMiOiJjb21wbGV0ZSIsImhvbGRlciI6eyJmaXJzdE5hbWUiOiJDb3JyaWUiLCJpbmZpeCI6InZhbiIsImxhc3ROYW1lIjoiR2VlciIsImJpcnRoRGF0ZSI6IjE5NjAtMDEtMDEifSwiZXZlbnRzIjpbeyJ0eXBlIjoidmFjY2luYXRpb24iLCJ1bmlxdWUiOiI0MmZkNTkyZi01YzA3LTRlMGItOGIwYi03YTk2OGQ5NzFjMmEiLCJpc1NwZWNpbWVuIjp0cnVlLCJ2YWNjaW5hdGlvbiI6eyJkYXRlIjoiMjAyMS0xMi0wOSIsImhwa0NvZGUiOiIyOTI0NTI4IiwidHlwZSI6IiIsIm1hbnVmYWN0dXJlciI6IiIsImJyYW5kIjoiIiwiY29tcGxldGVkQnlNZWRpY2FsU3RhdGVtZW50IjpmYWxzZSwiY29tcGxldGVkQnlQZXJzb25hbFN0YXRlbWVudCI6ZmFsc2UsImNvbXBsZXRpb25SZWFzb24iOm51bGwsImNvdW50cnkiOiJOTCIsImRvc2VOdW1iZXIiOm51bGwsInRvdGFsRG9zZXMiOm51bGx9fSx7InR5cGUiOiJ2YWNjaW5hdGlvbiIsInVuaXF1ZSI6ImViZDA3N2RhLTkyZjQtNDhjOC05ZmU1LWQ4MzlmZjIyMTA1NCIsImlzU3BlY2ltZW4iOnRydWUsInZhY2NpbmF0aW9uIjp7ImRhdGUiOiIyMDIyLTAxLTA4IiwiaHBrQ29kZSI6IjI5MjQ1MjgiLCJ0eXBlIjoiIiwibWFudWZhY3R1cmVyIjoiIiwiYnJhbmQiOiIiLCJjb21wbGV0ZWRCeU1lZGljYWxTdGF0ZW1lbnQiOmZhbHNlLCJjb21wbGV0ZWRCeVBlcnNvbmFsU3RhdGVtZW50IjpmYWxzZSwiY29tcGxldGlvblJlYXNvbiI6bnVsbCwiY291bnRyeSI6Ik5MIiwiZG9zZU51bWJlciI6bnVsbCwidG90YWxEb3NlcyI6bnVsbH19XX0=\"}".toByteArray()
        )
        holderDatabase.walletDao().insert(walletEntity)
        holderDatabase.eventGroupDao().insertAll(listOf(eventGroupEntity))

        val savedEvents = usecase.getSavedEvents()
        assertEquals(1, savedEvents.size)
        assertEquals("GGD", savedEvents.first().providerName)
        assertTrue(savedEvents.first().events.first().remoteEvent is RemoteEventVaccination)
    }

    @Test
    fun `Saved events are ordered from newest to oldest`() = runBlocking {
        val walletEntity = WalletEntity(
            id = 1,
            label = "main"
        )

        val eventGroupEntity = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "GGD",
            type = OriginType.Vaccination,
            scope = null,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "{\"signature\":\"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\",\"payload\":\"eyJwcm90b2NvbFZlcnNpb24iOiIzLjAiLCJwcm92aWRlcklkZW50aWZpZXIiOiJaWloiLCJzdGF0dXMiOiJjb21wbGV0ZSIsImhvbGRlciI6eyJmaXJzdE5hbWUiOiJDb3JyaWUiLCJpbmZpeCI6InZhbiIsImxhc3ROYW1lIjoiR2VlciIsImJpcnRoRGF0ZSI6IjE5NjAtMDEtMDEifSwiZXZlbnRzIjpbeyJ0eXBlIjoidmFjY2luYXRpb24iLCJ1bmlxdWUiOiI0MmZkNTkyZi01YzA3LTRlMGItOGIwYi03YTk2OGQ5NzFjMmEiLCJpc1NwZWNpbWVuIjp0cnVlLCJ2YWNjaW5hdGlvbiI6eyJkYXRlIjoiMjAyMS0xMi0xMyIsImhwa0NvZGUiOiIyOTI0NTI4IiwidHlwZSI6IiIsIm1hbnVmYWN0dXJlciI6IiIsImJyYW5kIjoiIiwiY29tcGxldGVkQnlNZWRpY2FsU3RhdGVtZW50IjpmYWxzZSwiY29tcGxldGVkQnlQZXJzb25hbFN0YXRlbWVudCI6ZmFsc2UsImNvbXBsZXRpb25SZWFzb24iOm51bGwsImNvdW50cnkiOiJOTCIsImRvc2VOdW1iZXIiOm51bGwsInRvdGFsRG9zZXMiOm51bGx9fSx7InR5cGUiOiJ2YWNjaW5hdGlvbiIsInVuaXF1ZSI6ImViZDA3N2RhLTkyZjQtNDhjOC05ZmU1LWQ4MzlmZjIyMTA1NCIsImlzU3BlY2ltZW4iOnRydWUsInZhY2NpbmF0aW9uIjp7ImRhdGUiOiIyMDIyLTAxLTEyIiwiaHBrQ29kZSI6IjI5MjQ1MjgiLCJ0eXBlIjoiIiwibWFudWZhY3R1cmVyIjoiIiwiYnJhbmQiOiIiLCJjb21wbGV0ZWRCeU1lZGljYWxTdGF0ZW1lbnQiOmZhbHNlLCJjb21wbGV0ZWRCeVBlcnNvbmFsU3RhdGVtZW50IjpmYWxzZSwiY29tcGxldGlvblJlYXNvbiI6bnVsbCwiY291bnRyeSI6Ik5MIiwiZG9zZU51bWJlciI6bnVsbCwidG90YWxEb3NlcyI6bnVsbH19XX0=\"}".toByteArray()
        )

        holderDatabase.walletDao().insert(walletEntity)
        holderDatabase.eventGroupDao().insertAll(listOf(eventGroupEntity))

        val savedEvents = usecase.getSavedEvents()
        val expectedFirstDate = OffsetDateTime.ofInstant(Instant.parse("2022-01-12T00:00:00.00Z"), ZoneId.of("UTC"))
        val expectedSecondDate = OffsetDateTime.ofInstant(Instant.parse("2021-12-13T00:00:00.00Z"), ZoneId.of("UTC"))

        assertEquals(expectedFirstDate, savedEvents.first().events[0].remoteEvent.getDate())
        assertEquals(expectedSecondDate, savedEvents.first().events[1].remoteEvent.getDate())
    }
}