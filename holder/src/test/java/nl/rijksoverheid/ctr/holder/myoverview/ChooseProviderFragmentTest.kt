package nl.rijksoverheid.ctr.holder.myoverview

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakePersistenceManager
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
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
class ChooseProviderFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setGraph(R.navigation.holder_nav_graph)
        it.setCurrentDestination(R.id.nav_choose_provider)
    }

    @Test
    fun `Clicking commercial button goes to date of birth screen when no date of birth is saved`() {
        launchChooseProviderFragment(
            hasDateOfBirthSaved = false
        )

        clickOn(R.id.provider_commercial)
        assertEquals(navController.currentDestination?.id, R.id.nav_date_of_birth_input)
    }

    @Test
    fun `Clicking commercial button goes to commercial test type screen when no date of birth is saved`() {
        launchChooseProviderFragment(
            hasDateOfBirthSaved = true
        )

        clickOn(R.id.provider_commercial)
        assertEquals(navController.currentDestination?.id, R.id.nav_commercial_test_type)
    }

    private fun launchChooseProviderFragment(hasDateOfBirthSaved: Boolean) {
        loadKoinModules(
            module(override = true) {
                factory {
                    fakePersistenceManager(
                        dateOfBirth = if (hasDateOfBirthSaved) 0L else null
                    )
                }
            }
        )

        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            ChooseProviderFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
