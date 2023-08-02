package nl.rijksoverheid.ctr.holder.pdf

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.fakeAndroidUtil
import nl.rijksoverheid.ctr.holder.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExportIntroductionFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_export_introduction)
    }

    @Test
    fun `bottom button navigates to pdf webview`() {
        launchFragment()

        clickOn(R.id.bottom)

        assertEquals(R.id.nav_pdf_webview, navController.currentDestination?.id)
    }

    @Test
    fun `image is hidden on screens with small height`() {
        loadKoinModules(
            module {
                factory { fakeAndroidUtil(isSmallScreen = true) }
            }
        )
        launchFragment()

        assertNotDisplayed(R.id.image)
    }

    @Test
    fun `image is displayed on screens with not small height`() {
        loadKoinModules(
            module {
                factory { fakeAndroidUtil(isSmallScreen = false) }
            }
        )
        launchFragment()

        assertDisplayed(R.id.image)
    }

    private fun launchFragment() {
        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            ExportIntroductionFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
