package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.adevinta.android.barista.assertion.BaristaListAssertions.assertCustomAssertionAtPosition
import com.adevinta.android.barista.assertion.BaristaListAssertions.assertListItemCount
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.internal.performActionOnView
import com.google.android.material.card.MaterialCardView
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeAppConfigViewModel
import nl.rijksoverheid.ctr.holder.fakeDashboardViewModel
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragment
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

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
    fun `Header should be displayed when dashboard header item is presented`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.HeaderItem(text = R.string.my_overview_description)
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemHeaderRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is ConstraintLayout }
            }
        )
    }

    @Test
    fun `Placeholder card should be displayed when placeholder item is presented`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.PlaceholderCardItem(GreenCardType.Domestic)
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemPlaceholderRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is MaterialCardView }
            }
        )
    }

    @Test
    fun `Clock deviation card should be displayed with a read more and without close button`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.ClockDeviationItem
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertNotDisplayed(R.id.close)
        assertDisplayed(R.id.button)
    }

    @Test
    fun `Non dismissible info card should be displayed when non dismissible item is presented`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.ExtendDomesticRecovery
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertNotDisplayed(R.id.close)
    }

    @Test
    fun `Info card should be displayed when dismissible item is presented and it can be dismissed`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.ExtendedDomesticRecovery
                )
            )
        )

        // assert card is displayed
        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertDisplayed(R.id.close)

        // dismiss card
        performActionOnView(ViewMatchers.withId(R.id.close), ViewActions.click())

        // assert card is dismissed
        assertListItemCount(listId = R.id.recyclerView, expectedItemCount = 0)
    }

    @Test
    fun `A single card should be displayed when 1 card item is presented`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.CardsItem(
                        listOf(
                            DashboardItem.CardsItem.CardItem(
                                greenCard = fakeGreenCard(),
                                originStates = listOf(),
                                credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                databaseSyncerResult = DatabaseSyncerResult.Success()
                            )
                        )
                    )
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof_1,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is MaterialCardView }
            }
        )
        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof_2,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view.height == 0 }
            }
        )
        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof_3,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view.height == 0 }
            }
        )
    }

    fun `Multiple cards should be displayed when the cards item has multiple cards`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.CardsItem(
                        listOf(
                            DashboardItem.CardsItem.CardItem(
                                greenCard = fakeGreenCard(),
                                originStates = listOf(),
                                credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                databaseSyncerResult = DatabaseSyncerResult.Success()
                            ),
                            DashboardItem.CardsItem.CardItem(
                                greenCard = fakeGreenCard(),
                                originStates = listOf(),
                                credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                databaseSyncerResult = DatabaseSyncerResult.Success()
                            ),
                            DashboardItem.CardsItem.CardItem(
                                greenCard = fakeGreenCard(),
                                originStates = listOf(),
                                credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                databaseSyncerResult = DatabaseSyncerResult.Success()
                            )
                        )
                    )
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof_2,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view.height > 0 }
            }
        )
        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof_3,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view.height > 0 }
            }
        )
    }

    @Test
    fun `Expired card no read more and can be dismissed`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.GreenCardExpiredItem(fakeGreenCard())
                )
            )
        )

        // assert display of card
        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertNotDisplayed(R.id.button)

        // dismiss card
        performActionOnView(ViewMatchers.withId(R.id.close), ViewActions.click())

        // assert card is dismissed
        assertListItemCount(listId = R.id.recyclerView, expectedItemCount = 0)
    }

    @Test
    fun `Origin card cannot be dismissed and should have a read more`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.OriginInfoItem(GreenCardType.Domestic, OriginType.Vaccination)
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertNotDisplayed(R.id.close)
        assertDisplayed(R.id.button)
    }

    @Test
    fun `Add qr button should be visible when its item is presented`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.AddQrButtonItem(true)
                )
            )
        )

        assertDisplayed(R.id.addQrButton)

        performActionOnView(ViewMatchers.withId(R.id.addQrButton), ViewActions.click())

        Assert.assertEquals(navController.currentDestination?.id, R.id.nav_qr_code_type)
    }

    @Test
    fun `Clicking Add qr button should navigate to qr code type`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.AddQrButtonItem(true)
                )
            )
        )

        performActionOnView(ViewMatchers.withId(R.id.addQrButton), ViewActions.click())

        Assert.assertEquals(navController.currentDestination?.id, R.id.nav_qr_code_type)
    }

    @Test
    fun `3G validity card cannot be dismissed and should have a read more`() {
        startFragment(
            DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.InfoItem.TestCertificate3GValidity
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemInfoRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is CardView }
            }
        )
        assertNotDisplayed(R.id.close)
        assertDisplayed(R.id.button)
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