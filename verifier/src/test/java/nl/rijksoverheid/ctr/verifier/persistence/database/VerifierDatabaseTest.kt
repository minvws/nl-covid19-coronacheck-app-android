package nl.rijksoverheid.ctr.verifier.persistence.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class HolderDatabaseTest : AutoCloseKoinTest() {

    private lateinit var db: VerifierDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VerifierDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        db.close()
    }

    @Test
    fun `Creating scan log works as expected`() = runBlocking {
        val scanLogToInsert1 = ScanLogEntity(
            policy = VerificationPolicy.VerificationPolicy1G,
            date = Instant.ofEpochSecond(0)
        )

        val scanLogToInsert2 = ScanLogEntity(
            policy = VerificationPolicy.VerificationPolicy3G,
            date = Instant.ofEpochSecond(1)
        )

        db.scanLogDao().insert(scanLogToInsert1)
        db.scanLogDao().insert(scanLogToInsert2)

        val expectedScanLog1 = ScanLogEntity(
            id = 1,
            policy = VerificationPolicy.VerificationPolicy1G,
            date = Instant.ofEpochSecond(0)
        )

        val expectedScanLog2 = ScanLogEntity(
            id = 2,
            policy = VerificationPolicy.VerificationPolicy3G,
            date = Instant.ofEpochSecond(1)
        )

        val scanLogsFromDb = db.scanLogDao().getAll()

        assertEquals(expectedScanLog1, scanLogsFromDb[0])
        assertEquals(expectedScanLog2, scanLogsFromDb[1])
    }

    @Test
    fun `Deleting scan log works as expected`() = runBlocking {
        val scanLogToInsert1 = ScanLogEntity(
            policy = VerificationPolicy.VerificationPolicy1G,
            date = Instant.ofEpochSecond(0)
        )

        val scanLogToInsert2 = ScanLogEntity(
            policy = VerificationPolicy.VerificationPolicy3G,
            date = Instant.ofEpochSecond(1)
        )

        db.scanLogDao().insert(scanLogToInsert1)
        db.scanLogDao().insert(scanLogToInsert2)
        db.scanLogDao().delete(listOf(db.scanLogDao().getAll().first()))

        val expectedScanLog2 = ScanLogEntity(
            id = 2,
            policy = VerificationPolicy.VerificationPolicy3G,
            date = Instant.ofEpochSecond(1)
        )

        val scanLogsFromDb = db.scanLogDao().getAll()

        assertEquals(scanLogsFromDb.size, 1)
        assertEquals(expectedScanLog2, scanLogsFromDb[0])
    }
}
