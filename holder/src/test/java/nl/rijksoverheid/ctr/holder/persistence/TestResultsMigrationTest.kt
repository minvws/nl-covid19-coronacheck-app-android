package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.fakeMobileCoreWrapper
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManagerImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class TestResultsMigrationTest: AutoCloseKoinTest() {
    private lateinit var db: HolderDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        db.close()
    }

    @Test
    fun `given stored credentials in the preferences, when they are migrated, they can be retrieved from the db`() = runBlocking {
        val persistenceManager = fakePersistenceManager()
        val testResultsMigrationManager = TestResultsMigrationManagerImpl(persistenceManager, fakeMobileCoreWrapper(), db)
        persistenceManager.saveCredentials("fakeCredentials")
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "main"
            )
        )

        testResultsMigrationManager.migrateTestResults()

        val cards = db.greenCardDao().getAll()
        assertEquals(1, cards.size)
    }

    private fun fakePersistenceManager() = object: PersistenceManager {
        override fun saveSecretKeyJson(json: String) = Unit

        override fun getSecretKeyJson() = null

        override fun saveCredentials(credentials: String) = Unit

        override fun getCredentials() = "fakeCredentials"

        override fun deleteCredentials() = Unit

        override fun hasSeenCameraRationale() = false

        override fun setHasSeenCameraRationale(hasSeen: Boolean) = Unit

        override fun hasDismissedRootedDeviceDialog() = true

        override fun setHasDismissedRootedDeviceDialog() = Unit

        override fun getSelectedGreenCardType(): GreenCardType {
            TODO("Not yet implemented")
        }

        override fun setSelectedGreenCardType(greenCardType: GreenCardType) {
            TODO("Not yet implemented")
        }

        override fun hasAppliedJune28Fix(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setJune28FixApplied(applied: Boolean) {
            TODO("Not yet implemented")
        }
    }
}

