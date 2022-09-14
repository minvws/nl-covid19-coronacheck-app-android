/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.usecases

import android.content.Context.MODE_PRIVATE
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.HolderApplication
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogResult
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogUseCaseImpl
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShowBlockedEventsDialogUseCaseImplTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase
    private lateinit var persistenceManager: PersistenceManager

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

        val sharedPreferences = context.getSharedPreferences(
            "prefs",
            MODE_PRIVATE
        )

        persistenceManager = SharedPreferencesPersistenceManager(sharedPreferences)
    }

    @Test
    fun `Show dialog if there are blocked events and local stored boolean is set to true`() = runBlocking {
        val blockedEventEntity = BlockedEventEntity(
            walletId = 1,
            type = "vaccination",
            eventTime = OffsetDateTime.now()
        )
        db.blockedEventDao().insert(blockedEventEntity)
        persistenceManager.setCanShowBlockedEventsDialog(true)

        val usecase = ShowBlockedEventsDialogUseCaseImpl(
            holderDatabase = db,
            persistenceManager = persistenceManager
        )

        val result = usecase.execute()
        assertTrue { result is ShowBlockedEventsDialogResult.Show }
        assertEquals(1, (result as ShowBlockedEventsDialogResult.Show).blockedEvents.size)
    }

    @Test
    fun `Do not show dialog if there are blocked events and local stored boolean is set to false`() = runBlocking {
        val blockedEventEntity = BlockedEventEntity(
            walletId = 1,
            type = "vaccination",
            eventTime = OffsetDateTime.now()
        )
        db.blockedEventDao().insert(blockedEventEntity)
        persistenceManager.setCanShowBlockedEventsDialog(false)

        val usecase = ShowBlockedEventsDialogUseCaseImpl(
            holderDatabase = db,
            persistenceManager = persistenceManager
        )

        val result = usecase.execute()
        assertTrue { result is ShowBlockedEventsDialogResult.None }
    }

    @Test
    fun `Do not show dialog if there are no blocked events`() = runBlocking {
        persistenceManager.setCanShowBlockedEventsDialog(true)

        val usecase = ShowBlockedEventsDialogUseCaseImpl(
            holderDatabase = db,
            persistenceManager = persistenceManager
        )

        val result = usecase.execute()
        assertTrue { result is ShowBlockedEventsDialogResult.None }
    }
}
