package nl.rijksoverheid.ctr.holder.myoverview

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.shared.livedata.Event
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

    @Test
    fun `Recyclerview has MyOverviewTestResultAdapterItem with header and 2 navigation card items`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.None
        )
        assertListItemCount(
            listId = R.id.recyclerView,
            expectedItemCount = 3
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.test_description,
            textId = R.string.my_overview_no_qr_description
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 1,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_appointment_title
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 2,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_qr_title
        )
    }

    private fun launchOverviewFragment(localTestResultState: LocalTestResultState) {
        val fakeLocalTestResultViewModel = object : LocalTestResultViewModel() {
            override fun getLocalTestResult() {
                localTestResultStateLiveData.value = Event(localTestResultState)
            }
        }

        loadKoinModules(
            module(override = true) {
                viewModel<LocalTestResultViewModel> { fakeLocalTestResultViewModel }
            }
        )

        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        navController.setGraph(R.navigation.holder_nav_graph)

        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            MyOverviewFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
