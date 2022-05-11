package nl.rijksoverheid.ctr.introduction.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import junit.framework.Assert.assertEquals
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeAppConfigViewModel
import nl.rijksoverheid.ctr.holder.fakeIntroductionViewModel
import nl.rijksoverheid.ctr.holder.fakeSetupViewModel
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetupFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.introduction_nav_graph)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `when setup is ongoing the progress is shown`() {
        startFragment(appStatus = AppStatus.UpdateRequired)

        assertDisplayed(R.id.splash_placeholder)
        assertDisplayed(R.id.progress)
        assertDisplayed(R.id.text, R.string.app_setup_text)
    }

    @Test
    fun `when config is updated it should navigate to onboarding`() {
        startFragment(appStatus = AppStatus.NoActionRequired)

        assertEquals(navController.currentDestination?.id, R.id.nav_onboarding)
    }

    private fun startFragment(appStatus: AppStatus): FragmentScenario<SetupFragment> {
        loadKoinModules(
            module(override = true) {
                viewModel { fakeAppConfigViewModel(appStatus = appStatus) }
                viewModel { fakeSetupViewModel() }
            })
        return launchFragmentInContainer(
            themeResId = R.style.AppTheme
        ) {
            SetupFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}