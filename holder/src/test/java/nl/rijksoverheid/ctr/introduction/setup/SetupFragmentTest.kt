package nl.rijksoverheid.ctr.introduction.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import org.junit.assertEquals
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.fakeSetupViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.fakeAppConfigViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class SetupFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.introduction_nav_graph)
    }

    private fun startSetupFragment(appStatus: AppStatus) {
        loadKoinModules(
            module(override = true) {
                viewModel { fakeAppConfigViewModel(appStatus) }
                viewModel { fakeSetupViewModel() }
            })
        launchFragmentInContainer(
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

    @Config(sdk = [23, 24, 25, 26, 27, 28, 29, 30])
    @Test
    fun `splash is shown in setup`() {
        startSetupFragment(appStatus = AppStatus.UpdateRequired)

        assertDisplayed(R.id.splash_placeholder)
    }

    @Test
    fun `when setup is ongoing the progress is shown`() {
        startSetupFragment(appStatus = AppStatus.UpdateRequired)

        assertDisplayed(R.id.progress)
        assertDisplayed(R.id.text, R.string.app_setup_text)
    }

    @Test
    fun `when config is updated it should navigate to onboarding`() {
        startSetupFragment(appStatus = AppStatus.NoActionRequired)

        assertEquals(navController.currentDestination?.id, R.id.nav_onboarding)
    }
}
