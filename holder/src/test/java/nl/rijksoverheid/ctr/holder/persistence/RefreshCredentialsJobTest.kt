package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import org.junit.Test

import org.junit.Assert.*
import androidx.work.testing.TestListenableWorkerBuilder
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class RefreshCredentialsJobTest {

    @Test
    fun `given a successful database sync, when worker does work, then it returns success`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<RefreshCredentialsJob>(context).setWorkerFactory(
            testWorkerFactory()
        ).build()

        val result = worker.startWork().get()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `given a unsuccessful database sync, when worker does work, then it returns retry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<RefreshCredentialsJob>(context).setWorkerFactory(
            testWorkerFactory(databaseSyncerResult = DatabaseSyncerResult.ServerError(500))
        ).build()

        val result = worker.startWork().get()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    private fun testWorkerFactory(
        expiringCardOriginType: OriginType? = OriginType.Test,
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success) = HolderWorkerFactory(
        greenCardsUseCase = object: GreenCardsUseCase {
            override suspend fun expiringCardOriginType(): OriginType? = expiringCardOriginType
        },
        holderDatabaseSyncer = object: HolderDatabaseSyncer {
            override suspend fun sync(
                expectedOriginType: String?,
                syncWithRemote: Boolean
            ): DatabaseSyncerResult = databaseSyncerResult
        }
    )
}