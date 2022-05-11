package nl.rijksoverheid.ctr.introduction.setup

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [23, 24, 25, 26, 27, 28, 29, 30])
@RunWith(RobolectricTestRunner::class)
class SetupFragmentTest: AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
    }

    private fun startSetupFragment() {
        loadKoinModules(
            module(override = true) {
                viewModel { mockk<AppConfigViewModel>(relaxed = true) }
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

    @Test
    fun `setup fragment test`() {
        startSetupFragment()

        assertDisplayed(R.id.splash_placeholder)
    }
}