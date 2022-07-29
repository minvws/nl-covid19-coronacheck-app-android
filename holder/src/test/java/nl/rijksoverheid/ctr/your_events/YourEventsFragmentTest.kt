/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.your_events

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeYourEventsViewModel
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragment
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YourEventsFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_your_events)
    }

    @Test
    fun `When database synced and no hints returned navigate to dashboard`() {
        startFragment(DatabaseSyncerResult.Success(listOf()))
        assertEquals(navController.currentDestination?.id, R.id.nav_dashboard)
    }

    @Test
    fun `When database synced and hints returned navigate to info fragment and show hints`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        startFragment(DatabaseSyncerResult.Success(listOf("app_name")))
        assertEquals(navController.currentDestination?.id, R.id.nav_info_fragment)
        val arguments = navController.backStack.last().arguments

        assertEquals("YourEventsFragmentTest", arguments?.getString("toolbarTitle"))
        val expectedData = InfoFragmentData.TitleDescriptionWithButton(
            title = context.getString(R.string.holder_eventHints_title),
            descriptionData = DescriptionData(
                htmlTextString = context.getString(R.string.app_name)
            ),
            primaryButtonData = ButtonData.NavigationButton(
                text = context.getString(R.string.general_toMyOverview),
                navigationActionId = R.id.action_my_overview
            )
        )
        val returnedData = arguments?.getParcelable<InfoFragmentData>("data")
        assertEquals(expectedData, returnedData)
    }

    @Test
    fun `When database synced and hints returned contain negativetest_without_vaccinationassessment navigate to info fragment with correct copy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        startFragment(DatabaseSyncerResult.Success(listOf("negativetest_without_vaccinationassessment")))
        assertEquals(navController.currentDestination?.id, R.id.nav_info_fragment)
        val arguments = navController.backStack.last().arguments

        assertEquals(context.getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_toolbar), arguments?.getString("toolbarTitle"))
        val expectedData = InfoFragmentData.TitleDescriptionWithButton(
            title = context.getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_title),
            descriptionData = DescriptionData(
                htmlText = R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_body
            ),
            primaryButtonData = ButtonData.NavigationButton(
                text = context.getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_button_complete),
                navigationActionId = R.id.action_visitor_pass_input_token
            )
        )
        val returnedData = arguments?.getParcelable<InfoFragmentData>("data")
        assertEquals(expectedData, returnedData)
    }

    @Test
    fun `When database synced failed with client network error show dialog`() {
        startFragment(DatabaseSyncerResult.Failed.Error(errorResult = NetworkRequestResult.Failed.ClientNetworkError(HolderStep.GetCredentialsNetworkRequest)))

        onView(withText(R.string.dialog_no_internet_connection_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    private fun startFragment(databaseSyncerResult: DatabaseSyncerResult): FragmentScenario<YourEventsFragment> {
        loadKoinModules(
            module(override = true) {
                viewModel { fakeYourEventsViewModel(databaseSyncerResult) }
            })
        val fragmentArgs = bundleOf(
            "toolbarTitle" to "YourEventsFragmentTest",
            "type" to YourEventsFragmentType.RemoteProtocol3Type(mapOf(), listOf()),
            "flow" to HolderFlow.Vaccination
        )
        return launchFragmentInContainer(
            fragmentArgs, themeResId = R.style.AppTheme
        ) {
            YourEventsFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
