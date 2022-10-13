/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.usecases

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.HolderApplication
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogResult
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShowBlockedEventsDialogUseCaseImplTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<HolderApplication>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "Wallet"
            )
        )
    }

    @Test
    fun `Show dialog if there are remote blocked events`() = runBlocking {
        val removedEventEntity = RemovedEventEntity(
            walletId = 1,
            type = "vaccination",
            eventTime = OffsetDateTime.now(),
            reason = RemovedEventReason.Blocked
        )
        db.removedEventDao().insert(removedEventEntity)

        val usecase = ShowBlockedEventsDialogUseCaseImpl(db)

        val result = usecase.execute(listOf(remoteEventVaccination()))
        assertTrue { result is ShowBlockedEventsDialogResult.Show }
        assertEquals(1, (result as ShowBlockedEventsDialogResult.Show).blockedEvents.size)
    }

    @Test
    fun `Do not show dialog if there are no remote blocked events`() = runBlocking {
        val usecase = ShowBlockedEventsDialogUseCaseImpl(
            holderDatabase = db
        )

        val result = usecase.execute(listOf())
        assertTrue { result is ShowBlockedEventsDialogResult.None }
    }

    private fun remoteEventVaccination(): RemoteEventVaccination {
        return RemoteEventVaccination(
            type = "vaccination",
            unique = "3ca0c918-5a20-4033-8fc3-8334cd5c63af",
            vaccination = RemoteEventVaccination.Vaccination(
                date = LocalDate.parse("2022-03-16"),
                hpkCode = "2924528",
                type = "",
                brand = "",
                completedByMedicalStatement = false,
                completedByPersonalStatement = false,
                completionReason = null,
                doseNumber = null,
                totalDoses = null,
                manufacturer = "",
                country = "NL"
            )
        )
    }
}
