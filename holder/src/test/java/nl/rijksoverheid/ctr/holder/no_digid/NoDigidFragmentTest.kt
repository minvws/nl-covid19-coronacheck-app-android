package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoDigidFragmentTest : AutoCloseKoinTest() {
    private val applicationContext: Context by lazy { ApplicationProvider.getApplicationContext() }
    private val intentUtil = mockk<IntentUtil>(relaxed = true)

    @Test
    fun `NoDigidFragment shows correct content`() {
        startFragment()

        assertDisplayed("screen title")
        assertDisplayed("screen description")
        assertDisplayed(R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD)
        assertDisplayed("button subtitle")
    }

    @Test
    fun `NoDigidFragment has working first button`() {
        startFragment()

        clickOn(R.id.first_button)

        verify { intentUtil.openUrl(any(), "url") }
    }

    @Test
    fun `NoDigidFragment has working second button`() {
        val navController = startFragment()

        clickOn(R.id.second_button)

        assertEquals(R.id.nav_no_digid, navController.currentDestination?.id)
    }

    private fun startFragment(): NavHostController {
        loadKoinModules(
            module(override = true) {
                factory { intentUtil }
            }
        )
        val fragmentArgs = bundleOf(
            "data" to NoDigidFragmentData(
                title = "screen title",
                description = "screen description",
                firstNavigationButtonData = NoDigidNavigationButtonData.Link(R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD, "button subtitle", null, "url"),
                secondNavigationButtonData = NoDigidNavigationButtonData.NoDigid(R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD, null, R.drawable.ic_digid_logo, mockk()),
                originType = RemoteOriginType.Vaccination
            ),
            "toolbarTitle" to "toolbar title"
        )

        val navController = TestNavHostController(applicationContext)

        val getEventsScenario = launchFragmentInContainer<NoDigidFragment>(
            fragmentArgs, themeResId = R.style.AppTheme
        )

        getEventsScenario.onFragment {
            navController.setGraph(R.navigation.holder_nav_graph_main)
            navController.setCurrentDestination(R.id.nav_no_digid)
            Navigation.setViewNavController(it.requireView(), navController)
        }

        return navController
    }
}
