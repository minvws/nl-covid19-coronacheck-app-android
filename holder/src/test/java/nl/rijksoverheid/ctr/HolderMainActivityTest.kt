/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.findNavController
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
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
class HolderMainActivityTest : AutoCloseKoinTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<HolderMainActivity>

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `If consent needed navigate to app status`() {
        val scenario = launchHolderMainActivity(
            appStatus = AppStatus.ConsentNeeded(AppUpdateData(listOf(), NewTerms(1, true)))
        )
        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_app_locked
            )
        }
    }

    @Test
    fun `If new features navigate to app status`() {
        val scenario = launchHolderMainActivity(
            appStatus = AppStatus.NewFeatures(AppUpdateData(listOf(), NewTerms(1, true)))
        )
        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_app_locked
            )
        }
    }

    @Test
    fun `If app status is not NoActionRequired navigate to app status`() {
        val scenario = launchHolderMainActivity(
            appStatus = AppStatus.Error
        )
        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_app_locked
            )
        }
    }

    private fun launchHolderMainActivity(
        appStatus: AppStatus = AppStatus.NoActionRequired
    ): ActivityScenario<HolderMainActivity> {

        loadKoinModules(
            module {
                viewModel {
                    fakeAppConfigViewModel(
                        appStatus = appStatus
                    )
                }
                factory {
                    fakeCachedAppConfigUseCase()
                }
                single {
                    val context = ApplicationProvider.getApplicationContext<Context>()
                    Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).allowMainThreadQueries().build()
                }
            })

        scenario = ActivityScenario.launch(
            Intent(
                ApplicationProvider.getApplicationContext(),
                HolderMainActivity::class.java
            )
        )
        return scenario
    }
}
