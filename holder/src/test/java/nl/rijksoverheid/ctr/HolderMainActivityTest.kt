/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
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
    fun `If app status is not NoActionRequired navigate to app status`() {
        val scenario = launchHolderMainActivity(
            appStatus = AppStatus.Deactivated
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
                    object: AppConfigViewModel() {

                    }
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
