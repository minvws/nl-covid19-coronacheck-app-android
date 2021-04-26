package nl.rijksoverheid.ctr.holder

import android.content.Intent
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import org.junit.After
import org.junit.Assert.assertEquals
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

    private lateinit var scenario: ActivityScenario<HolderMainActivity>

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `If introduction not finished navigate to introduction`() {
        launchHolderMainActivity(
            introductionStatus = IntroductionStatus.IntroductionNotFinished(
                IntroductionData(
                    onboardingItems = listOf(),
                    privacyPolicyItems = listOf(),
                    null
                )
            )
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_introduction
            )
        }
    }

    @Test
    fun `If introduction finished navigate to main`() {
        val scenario = launchHolderMainActivity()
        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_main
            )
        }
    }

    @Test
    fun `If app status is not NoActionRequired navigate to app status`() {
        val scenario = launchHolderMainActivity(
            appStatus = AppStatus.InternetRequired
        )
        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_app_status
            )
        }
    }

    private fun launchHolderMainActivity(
        introductionStatus: IntroductionStatus = IntroductionStatus.IntroductionFinished.NoActionRequired,
        appStatus: AppStatus = AppStatus.NoActionRequired
    ): ActivityScenario<HolderMainActivity> {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeIntroductionViewModel(
                        introductionStatus = introductionStatus
                    )
                }
                viewModel {
                    fakeAppConfigViewModel(
                        appStatus = appStatus
                    )
                }
                factory {
                    fakeSecretKeyUseCase()
                }
                factory {
                    fakeCachedAppConfigUseCase()
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
