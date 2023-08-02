package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result.Failure
import androidx.work.ListenableWorker.Result.Retry
import androidx.work.ListenableWorker.Result.Success
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import org.junit.Assert.assertTrue
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
class CredentialRefreshWorkerTest : AutoCloseKoinTest() {
    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>().apply {
        coEvery { getCachedAppConfig().appDeactivated } returns false
    }

    private val holderFeatureFlagUseCase = mockk<HolderFeatureFlagUseCase>().apply {
        coEvery { isInArchiveMode() } returns false
    }

    @Test
    fun `when config fetch fails, credentials refresh worker fails`() = runBlocking {
        val configResultUseCase = mockk<ConfigResultUseCase>().apply {
            coEvery { fetch() } returns ConfigResult.Error(mockk(relaxed = true))
        }
        val holderDatabaseSyncer = mockk<HolderDatabaseSyncer>()

        val worker = CredentialRefreshWorker(
            context,
            mockk(relaxed = true),
            configResultUseCase,
            cachedAppConfigUseCase,
            holderFeatureFlagUseCase,
            holderDatabaseSyncer
        )

        assertTrue(worker.doWork() is Failure)
    }

    @Test
    fun `when app is deactivated, credentials refresh worker should not retry`() = runBlocking {
        val configResultUseCase = mockk<ConfigResultUseCase>().apply {
            coEvery { fetch() } returns ConfigResult.Success("", "")
        }
        val holderDatabaseSyncer = mockk<HolderDatabaseSyncer>()

        val worker = CredentialRefreshWorker(
            context,
            mockk(relaxed = true),
            configResultUseCase,
            mockk<CachedAppConfigUseCase>().apply {
                coEvery { getCachedAppConfig().appDeactivated } returns true
            },
            holderFeatureFlagUseCase,
            holderDatabaseSyncer
        )

        assertTrue(worker.doWork() !is Retry)
    }

    private fun succesfullConfigFetchWithSync(
        holderDatabaseSyncerResult: DatabaseSyncerResult
    ) = CredentialRefreshWorker(
        context,
        mockk(relaxed = true),
        mockk<ConfigResultUseCase>().apply {
            coEvery { fetch() } returns ConfigResult.Success("", "")
        },
        cachedAppConfigUseCase,
        holderFeatureFlagUseCase,
        mockk<HolderDatabaseSyncer>().apply {
            coEvery {
                sync(
                    flow = HolderFlow.SyncGreenCards,
                    syncWithRemote = true
                )
            } returns holderDatabaseSyncerResult
        }
    )

    @Test
    fun `when sync fails due to network error, credentials refresh worker should retry`() =
        runBlocking {
            val worker = succesfullConfigFetchWithSync(
                DatabaseSyncerResult.Failed.NetworkError(
                    mockk(),
                    true
                )
            )

            assertTrue(worker.doWork() is Retry)
        }

    @Test
    fun `when sync fails once due to server error, credentials refresh worker should retry`() =
        runBlocking {
            val worker = succesfullConfigFetchWithSync(
                DatabaseSyncerResult.Failed.ServerError.FirstTime(mockk())
            )

            assertTrue(worker.doWork() is Retry)
        }

    @Test
    fun `when sync fails many times due to server error, credentials refresh worker should fail`() =
        runBlocking {
            val worker = succesfullConfigFetchWithSync(
                DatabaseSyncerResult.Failed.ServerError.MultipleTimes(mockk())
            )

            assertTrue(worker.doWork() is Failure)
        }

    @Test
    fun `when sync fails, credentials refresh worker should fail`() = runBlocking {
        val worker = succesfullConfigFetchWithSync(DatabaseSyncerResult.Failed.Error(mockk()))

        assertTrue(worker.doWork() is Failure)
    }

    @Test
    fun `when sync succeeds, credentials refresh worker should succeed`() = runBlocking {
        val worker = succesfullConfigFetchWithSync(DatabaseSyncerResult.Success(listOf()))

        assertTrue(worker.doWork() is Success)
    }
}
