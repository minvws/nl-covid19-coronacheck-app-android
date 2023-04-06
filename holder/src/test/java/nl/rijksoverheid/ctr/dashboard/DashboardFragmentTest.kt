package nl.rijksoverheid.ctr.dashboard

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
import kotlin.test.assertTrue
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.fakeAppConfigViewModel
import nl.rijksoverheid.ctr.fakeDashboardViewModel
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.DashboardFragment
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert
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
class DashboardFragmentTest : AutoCloseKoinTest() {

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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.HeaderItem(text = R.string.my_overview_description, null)
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.PlaceholderCardItem(GreenCardType.Eu)
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.ClockDeviationItem
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.ConfigFreshnessWarning(0L)
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.GreenCardExpiredItem(
                            GreenCardType.Eu, fakeOriginEntity()
                        )
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.CardsItem(
                            listOf(
                                DashboardItem.CardsItem.CardItem(
                                    greenCard = fakeGreenCard(),
                                    originStates = listOf(),
                                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                                    disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
                                    greenCardEnabledState = GreenCardEnabledState.Enabled
                                )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.CardsItem(
                            listOf(
                                DashboardItem.CardsItem.CardItem(
                                    greenCard = fakeGreenCard(),
                                    originStates = listOf(),
                                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                                    disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
                                    greenCardEnabledState = GreenCardEnabledState.Enabled
                                ),
                                DashboardItem.CardsItem.CardItem(
                                    greenCard = fakeGreenCard(),
                                    originStates = listOf(),
                                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                                    disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
                                    greenCardEnabledState = GreenCardEnabledState.Enabled
                                ),
                                DashboardItem.CardsItem.CardItem(
                                    greenCard = fakeGreenCard(),
                                    originStates = listOf(),
                                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                                    disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
                                    greenCardEnabledState = GreenCardEnabledState.Enabled
                                )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.GreenCardExpiredItem(
                            greenCardType = fakeGreenCard().greenCardEntity.type,
                            originEntity = fakeOriginEntity(type = OriginType.Vaccination)
                        )
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.OriginInfoItem(
                            GreenCardType.Eu,
                            OriginType.Vaccination
                        )
                    )
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
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.AddQrButtonItem
                    )
                )
            )
        )

        assertDisplayed(R.id.addQrButton)

        performActionOnView(ViewMatchers.withId(R.id.addQrButton), ViewActions.click())

        Assert.assertEquals(navController.currentDestination?.id, R.id.nav_choose_proof_type)
    }

    @Test
    fun `Clicking Add qr button should navigate to choose proof type`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.AddQrButtonItem
                    )
                )
            )
        )

        performActionOnView(ViewMatchers.withId(R.id.addQrButton), ViewActions.click())

        Assert.assertEquals(navController.currentDestination?.id, R.id.nav_choose_proof_type)
    }

    @Test
    fun `Ad qr card should be displayed when add qr card item is presented`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.AddQrCardItem
                    )
                )
            )
        )

        assertCustomAssertionAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.dashboardItemAddQrCardRoot,
            viewAssertion = ViewAssertion { view, _ ->
                assertTrue { view is MaterialCardView }
            }
        )
        assertDisplayed(R.id.text, R.string.holder_dashboard_addCard_title)
    }

    @Test
    fun `Clicking Add qr card should navigate to choose proof type`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.AddQrCardItem
                    )
                )
            )
        )

        performActionOnView(ViewMatchers.withId(R.id.text), ViewActions.click())

        Assert.assertEquals(navController.currentDestination?.id, R.id.nav_choose_proof_type)
    }

    @Test
    fun `policy info card can be dismissed and should have a read more`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.DisclosurePolicyItem(DisclosurePolicy.ThreeG)
                    )
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
        assertDisplayed(R.id.close)
        assertDisplayed(R.id.button)
    }

    @Test
    fun `policy info for 3G should be shown on 3G disclosure policy`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.DisclosurePolicyItem(DisclosurePolicy.ThreeG)
                    )
                )
            )
        )

        assertDisplayed(R.id.text, R.string.holder_dashboard_only3GaccessBanner_title)
    }

    @Test
    fun `policy info for 1G should be shown on 1G disclosure policy`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.DisclosurePolicyItem(DisclosurePolicy.OneG)
                    )
                )
            )
        )

        assertDisplayed(R.id.text, R.string.holder_dashboard_only1GaccessBanner_title)
    }

    @Test
    fun `policy info for 1G+3G should be shown on 1G+3G disclosure policy`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf(
                        DashboardItem.InfoItem.DisclosurePolicyItem(DisclosurePolicy.OneAndThreeG)
                    )
                )
            )
        )

        assertDisplayed(R.id.text, R.string.holder_dashboard_3Gand1GaccessBanner_title)
    }

    @Test
    fun `tabs are hidden if there is only one tab to display`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf()
                )
            )
        )

        assertNotDisplayed(R.id.tabs)
        assertNotDisplayed(R.id.tabs_separator)
    }

    @Test
    fun `tabs are visible if there is more than one tab to display`() {
        startFragment(
            listOf(
                DashboardTabItem(
                    title = R.string.travel_button_domestic,
                    greenCardType = GreenCardType.Eu,
                    items = listOf()
                ),
                DashboardTabItem(
                    title = R.string.travel_button_europe,
                    greenCardType = GreenCardType.Eu,
                    items = listOf()
                )
            )
        )

        assertDisplayed(R.id.tabs)
        assertDisplayed(R.id.tabs_separator)
    }

    private fun startFragment(tabItems: List<DashboardTabItem>): FragmentScenario<DashboardFragment> {
        loadKoinModules(
            module {
                viewModel { fakeAppConfigViewModel(appStatus = AppStatus.NoActionRequired) }
                viewModel { fakeDashboardViewModel(tabItems) }
            })
        val fragmentArgs = bundleOf(
            "returnUri" to "test"
        )
        return launchFragmentInContainer(
            fragmentArgs, themeResId = R.style.AppTheme
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
}
