package nl.rijksoverheid.ctr.verifier

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
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

@RunWith(RobolectricTestRunner::class)
class VerifierMainActivityTest : AutoCloseKoinTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<VerifierMainActivity>

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `On app launch call cleanups`() {
        val verifierMainActivityViewModel = mockk<VerifierMainActivityViewModel>(relaxed = true)
        launchVerifierMainActivity(
            fakeIntroductionViewModel(
                introductionStatus = IntroductionStatus.OnboardingNotFinished(
                    introductionData = IntroductionData(
                        onboardingItems = listOf(),
                        privacyPolicyItems = listOf()
                    )
                ),
            ),
            verifierMainActivityViewModel = verifierMainActivityViewModel
        )

        verify { verifierMainActivityViewModel.cleanup() }
    }

    @Test
    fun `If introduction not finished navigate to introduction`() {
        val scenario = launchVerifierMainActivity(
            fakeIntroductionViewModel(
                introductionStatus = IntroductionStatus.OnboardingNotFinished(
                    introductionData = IntroductionData(
                        onboardingItems = listOf(),
                        privacyPolicyItems = listOf()
                    )
                ),
            ),
            verifierMainActivityViewModel = mockk(relaxed = true)
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_onboarding
            )
        }
    }

    @Test
    fun `If introduction finished navigate to main`() {
        val scenario = launchVerifierMainActivity(
            fakeIntroductionViewModel(setupRequired = false),
            verifierMainActivityViewModel = mockk(relaxed = true)
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_main
            )
        }
    }

    @Test
    fun `If consent needed navigate to new terms`() {
        val scenario = launchVerifierMainActivity(
            fakeIntroductionViewModel(setupRequired = false),
            verifierMainActivityViewModel = mockk(relaxed = true),
            appStatus = AppStatus.ConsentNeeded(AppUpdateData(listOf(), NewTerms(1, true)))
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_new_terms
            )
        }
    }

    @Test
    fun `If new features navigate to introduction`() {
        val scenario = launchVerifierMainActivity(
            fakeIntroductionViewModel(setupRequired = false),
            verifierMainActivityViewModel = mockk(relaxed = true),
            appStatus = AppStatus.NewFeatures(AppUpdateData(listOf(), NewTerms(1, true)))
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_new_features
            )
        }
    }

    @Test
    fun `If app status is not NoActionRequired navigate to app status`() {
        val scenario = launchVerifierMainActivity(
            fakeIntroductionViewModel(),
            appStatus = AppStatus.Error,
            verifierMainActivityViewModel = mockk(relaxed = true)
        )

        scenario.onActivity {
            assertEquals(
                it.findNavController(R.id.main_nav_host_fragment).currentDestination?.id,
                R.id.nav_app_locked
            )
        }
    }

    private fun launchVerifierMainActivity(
        introductionViewModel: IntroductionViewModel,
        appStatus: AppStatus = AppStatus.NoActionRequired,
        verifierMainActivityViewModel: VerifierMainActivityViewModel,
    ): ActivityScenario<VerifierMainActivity> {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    introductionViewModel
                }
                viewModel {
                    fakeAppConfigViewModel(
                        appStatus = appStatus
                    )
                }
                viewModel {
                    verifierMainActivityViewModel
                }
                factory {
                    fakeCachedAppConfigUseCase()
                }
                factory {
                    fakeMobileCoreWrapper()
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
