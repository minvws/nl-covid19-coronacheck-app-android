package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.RefreshState
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.shared.models.Environment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class WorkManagerIntegrationTests : AutoCloseKoinTest() {

    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val initialDelay = 3L

    private val greenCardRefreshUtil = mockk<GreenCardRefreshUtil>().apply {
        coEvery { refreshState() } returns RefreshState.Refreshable(initialDelay)
    }

    private val internationalQRRelevancyDays = 2L

    private val appConfigUseCase = mockk<HolderCachedAppConfigUseCase>().apply {
        every { getCachedAppConfig().internationalQRRelevancyDays } returns internationalQRRelevancyDays.toInt()
    }

    @Before
    fun setup() {
        loadKoinModules(
            module(override = true) {
                factory {
                    mockk<ConfigResultUseCase>().apply {
                        coEvery { fetch() } returns ConfigResult.Success("", "")
                    }
                }
                factory {
                    mockk<HolderDatabaseSyncer>().apply {
                        coEvery {
                            sync(
                                flow = HolderFlow.SyncGreenCards,
                                syncWithRemote = true,
                            )
                        } returns DatabaseSyncerResult.Success(false, mockk())
                    }
                }

            })

        val holderWorkerFactory: WorkerFactory by inject()

        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(holderWorkerFactory)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun test() = runBlocking {
        val workerManagerUtil =
            WorkerManagerUtilImpl(context, greenCardRefreshUtil, appConfigUseCase, Environment.Prod)

        val request = workerManagerUtil.scheduleRefreshCredentialsJob()!!

        // test work setup
        val workSpec = request.workSpec
        assertEquals(internationalQRRelevancyDays, TimeUnit.MILLISECONDS.toDays(workSpec.intervalDuration))
        assertEquals(initialDelay, TimeUnit.MILLISECONDS.toDays(workSpec.initialDelay))
        assertEquals(BackoffPolicy.EXPONENTIAL, workSpec.backoffPolicy)
        assertTrue(workSpec.isPeriodic)

        // test work is enqueued
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))

        //test work is running when conditions are met
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setAllConstraintsMet(request.id)
        testDriver.setInitialDelayMet(request.id)
        val updatedWorkInfo = workManager.getWorkInfoById(request.id).get()

        assertThat(updatedWorkInfo.state, `is`(WorkInfo.State.RUNNING))
    }
}