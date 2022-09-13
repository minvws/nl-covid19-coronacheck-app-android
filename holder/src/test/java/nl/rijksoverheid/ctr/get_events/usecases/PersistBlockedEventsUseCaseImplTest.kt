/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.get_events.usecases

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.InternalPlatformDsl.toStr
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.fakeRemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.usecases.PersistBlockedEventsUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersistBlockedEventsUseCaseImplTest : AutoCloseKoinTest() {

    @Test
    fun `Only persist events that are not new events`() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), HolderDatabase::class.java).build()
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "Wallet"
            )
        )

        val usecase = PersistBlockedEventsUseCaseImpl(db)

        val firstVaccinationEvent = fakeRemoteEventVaccination(date = LocalDate.of(2022, 1, 1))
        val secondVaccinationEvent = fakeRemoteEventVaccination(date = LocalDate.of(2022, 1, 2))

        usecase.persist(
            newEvents = listOf(firstVaccinationEvent),
            blockedEvents = listOf(firstVaccinationEvent, secondVaccinationEvent)
        )

        // Only the secondVaccinationEvent should be persisted,
        val blockedEvents = db.blockedEventDao().getAll()
        assertEquals(1, blockedEvents.size)
        assertEquals("2022-01-02T00:00Z", blockedEvents.first().eventTime.toStr())

        db.close()
    }
}
