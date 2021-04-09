package nl.rijksoverheid.ctr.verifier

import android.content.Intent
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifierMainActivityTest : AutoCloseKoinTest() {

    private lateinit var scenario: ActivityScenario<VerifierMainActivity>

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `If introduction not finished navigate to introduction`() {
        val scenario = launchHolderMainActivity(
            introductionFinished = false
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
        val scenario = launchHolderMainActivity(
            introductionFinished = true
        )
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
        introductionFinished: Boolean = true,
        appStatus: AppStatus = AppStatus.NoActionRequired
    ): ActivityScenario<VerifierMainActivity> {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeIntroductionViewModel(
                        introductionFinished = introductionFinished
                    )
                }
                viewModel {
                    fakeAppConfigViewModel(
                        appStatus = appStatus
                    )
                }
                factory {
                    fakeCachedAppConfigUseCase()
                }
            })

        scenario = ActivityScenario.launch(
            Intent(
                ApplicationProvider.getApplicationContext(),
                VerifierMainActivity::class.java
            )
        )
        return scenario
    }
}
