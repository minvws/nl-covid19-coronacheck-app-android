package nl.rijksoverheid.ctr.holder.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result.Retry
import androidx.work.ListenableWorker.Result.Success
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
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
class ConfigFetchWorkerTest : AutoCloseKoinTest() {
    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `when config fetch fails, config fetch worker should retry`() = runBlocking {
        val configResultUseCase = mockk<ConfigResultUseCase>().apply {
            coEvery { fetch() } returns ConfigResult.Error
        }

        val worker = ConfigFetchWorker(context, mockk(relaxed = true), configResultUseCase)

        assertTrue(worker.doWork() is Retry)
    }

    @Test
    fun `when config fetch succeeds, config fetch worker succeeds`() = runBlocking {
        val configResultUseCase = mockk<ConfigResultUseCase>().apply {
            coEvery { fetch() } returns ConfigResult.Success("", "")
        }

        val worker = ConfigFetchWorker(context, mockk(relaxed = true), configResultUseCase)

        assertTrue(worker.doWork() is Success)
    }
}
