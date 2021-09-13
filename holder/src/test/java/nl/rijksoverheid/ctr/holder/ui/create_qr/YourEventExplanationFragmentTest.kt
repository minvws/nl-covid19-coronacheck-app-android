package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.InfoScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class YourEventExplanationFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_your_events)
    }

    @Test
    fun `given one vaccination event, when presented, then no divider line is shown`() {
        val fragmentScenario = startFragment(
            arrayOf(
                InfoScreen(
                    title = "title",
                    description = "description",
                )
            )
        )

        fragmentScenario.withFragment {
            assertEquals(
                0,
                requireActivity().findViewById<RecyclerView>(R.id.scroll).itemDecorationCount
            )
        }
    }

    private fun startFragment(events: Array<InfoScreen>): FragmentScenario<YourEventExplanationFragment> {
        val fragmentArgs = bundleOf(
            "data" to events,
            "toolbarTitle" to "toolbarTitle"
        )
        return launchFragmentInContainer(
            fragmentArgs, themeResId = R.style.AppTheme
        ) {
            YourEventExplanationFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}