/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaHintAssertions.assertHint
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeCommercialTestResultViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CommercialTestCodeFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_commercial_test_code)
    }

    @Test
    fun `Correct default values shown when entering without deeplink`() {
        launchInputFragment()
        assertDisplayed(R.id.description, R.string.commercial_test_code_description)
        assertDisplayed(R.id.unique_code_input)
        assertHint(R.id.unique_code_input, R.string.commercial_test_unique_code_header)
        assertEnabled(R.id.button)
    }

    private fun launchInputFragment(token: String? = null) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeCommercialTestResultViewModel()
                }
            }
        )

        launchFragmentInContainer(
            // Supply navArgs
            bundleOf(
                "token" to token,
            ), themeResId = R.style.AppTheme
        ) {
            CommercialTestCodeFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
