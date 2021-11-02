package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeAppConfigViewModel
import nl.rijksoverheid.ctr.holder.fakeDashboardViewModel
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragment
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
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
class MyOverviewFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `header item`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.HeaderItem(text = R.string.my_overview_description)
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.text,
            textId = R.string.my_overview_description
        )
    }

    private fun startFragment(tabItem: DashboardTabItem): FragmentScenario<MyOverviewTabsFragment> {
        loadKoinModules(
            module(override = true) {
                viewModel { fakeAppConfigViewModel(appStatus = AppStatus.NoActionRequired) }
                viewModel { fakeDashboardViewModel(listOf(tabItem)) }
            })
        val fragmentArgs = bundleOf(
            "returnUri" to "test",
        )
        return launchFragmentInContainer(
            fragmentArgs, themeResId = R.style.AppTheme
        ) {
            MyOverviewTabsFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}