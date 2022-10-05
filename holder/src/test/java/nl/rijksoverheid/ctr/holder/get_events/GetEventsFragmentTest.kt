package nl.rijksoverheid.ctr.holder.get_events

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.shared.livedata.Event
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
class GetEventsFragmentTest : AutoCloseKoinTest() {

    private val applicationContext: Context by lazy { ApplicationProvider.getApplicationContext() }

    @Test
    fun `clicking on no digid button opens NoDigidFragment with correct data`() {
        val navController = startFragment()

        clickOn(R.id.no_digid_button)

        assertEquals(R.id.nav_no_digid, navController.currentDestination?.id)
    }

    @Test
    fun `show loading indicator while loading events`() {
        val fakeViewModel = fakeViewModel()

        startFragment(fakeViewModel)
        (fakeViewModel.loading as MutableLiveData).value = Event(true)

        assertDisplayed(R.string.holder_fetchevents_loading)
        assertDisplayed(R.id.progress_bar)
    }

    @Test
    fun `hide loading indicator while not loading events`() {
        val fakeViewModel = fakeViewModel()

        startFragment(fakeViewModel)
        (fakeViewModel.loading as MutableLiveData).value = Event(false)

        assertNotDisplayed(R.string.holder_fetchevents_loading)
        assertNotDisplayed(R.id.progress_bar)
    }

    private fun fakeViewModel() = object : GetEventsViewModel() {
        override fun getDigidEvents(
            loginType: LoginType,
            jwt: String,
            originTypes: List<RemoteOriginType>,
            getPositiveTestWithVaccination: Boolean
        ) {
            TODO("Not yet implemented")
        }

        override fun getMijnCnEvents(
            jwt: String,
            originType: RemoteOriginType,
            getPositiveTestWithVaccination: Boolean
        ) {
            TODO("Not yet implemented")
        }
    }

    private fun startFragment(
        getEventsViewModel: GetEventsViewModel = fakeViewModel()
    ): NavHostController {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    getEventsViewModel
                }
            }
        )

        val fragmentArgs = bundleOf(
            "originType" to RemoteOriginType.Vaccination,
            "toolbarTitle" to applicationContext.getString(R.string.add_paper_proof)
        )

        val navController = TestNavHostController(applicationContext)

        val getEventsScenario = launchFragmentInContainer<GetEventsFragment>(
            fragmentArgs, themeResId = R.style.AppTheme
        )

        getEventsScenario.onFragment {
            navController.setGraph(R.navigation.holder_nav_graph_main)
            navController.setCurrentDestination(R.id.nav_get_events)
            Navigation.setViewNavController(it.requireView(), navController)
        }

        return navController
    }
}
