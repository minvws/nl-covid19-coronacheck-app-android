package nl.rijksoverheid.ctr.holder.dashboard.items

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaClickableAssertions.assertNotClickable
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.DashboardFragment
import nl.rijksoverheid.ctr.holder.dashboard.DashboardViewModel
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DashboardPageFragmentTest : AutoCloseKoinTest() {

    private val viewModel = mockk<DashboardViewModel>(relaxed = true)
    private val dashboardTabItemsLiveData: MutableLiveData<List<DashboardTabItem>> =
        MutableLiveData()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_your_events)
    }

    private fun launch() {
        every { viewModel.dashboardTabItemsLiveData } returns dashboardTabItemsLiveData

        loadKoinModules(
            module(override = true) {
                viewModel {
                    viewModel
                }
            }
        )

        launchFragmentInContainer(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundleOf("returnUri" to null)
        ) {
            DashboardFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }

                }
            }
        }
    }

    @Test
    fun `dashboard header item is visible with correct copy and is not announced as interactive element`() {
        dashboardTabItemsLiveData.postValue(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_europe,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.HeaderItem(
                            R.string.holder_dashboard_filledState_international_0G_message,
                            ButtonInfo(
                                R.string.my_overview_description_eu_button_text,
                                R.string.my_overview_description_eu_button_link
                            )
                        )
                    )
                )
            )
        )

        launch()

        assertDisplayed(R.string.holder_dashboard_filledState_international_0G_message)
        assertNotClickable(R.string.holder_dashboard_filledState_international_0G_message)
    }
}