package nl.rijksoverheid.ctr.holder.persistence

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.CardUiLogic
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.GreenCardErrorState
import org.junit.Assert.assertEquals
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
class RefreshCredentialsJobTest: AutoCloseKoinTest() {

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
        databaseSyncerResult: DatabaseSyncerResult = DatabaseSyncerResult.Success) = HolderWorkerFactory(
        greenCardsUseCase = object: GreenCardsUseCase {
            override suspend fun faultyVaccinationsJune28(): Boolean {
                return false
            }

            override suspend fun expiring(): Boolean = true
            override suspend fun expiredCard(selectedType: GreenCardType): Boolean {
                TODO("Not yet implemented")
            }

            override suspend fun firstExpiringCard() = GreenCard.Expiring(4L)

            override suspend fun refresh(
                handleErrorOnExpiringCard: suspend (DatabaseSyncerResult) -> GreenCardErrorState,
                showForcedError: CardUiLogic,
                showRefreshError: CardUiLogic,
                showCardLoading: CardUiLogic
            ): GreenCardErrorState {
                TODO("Not yet implemented")
            }
        },
        holderDatabaseSyncer = object: HolderDatabaseSyncer {
            override suspend fun sync(
                expectedOriginType: OriginType?,
                syncWithRemote: Boolean
            ): DatabaseSyncerResult = databaseSyncerResult
        }
    )
}