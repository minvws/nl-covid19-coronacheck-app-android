package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.holder.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
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
class CommercialTestTypeFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_commercial_test_type)
    }

    @Test
    fun `Code button navigates to code input screen`() {
        launchCommercialTestTypeFragment()
        clickOn(R.id.type_code)
        assertEquals(navController.currentDestination?.id, R.id.nav_commercial_test_code)
    }

    @Test
    fun `No code button navigates to no code dialog`() {
        launchCommercialTestTypeFragment()
        clickOn(R.id.no_code_button)
        assertEquals(navController.currentDestination?.id, R.id.nav_no_code)
    }

    private fun launchCommercialTestTypeFragment() {
        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            CommercialTestTypeFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

}
