package nl.rijksoverheid.ctr.holder.myoverview

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItemChild
import io.mockk.InternalPlatformDsl.toStr
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.OffsetDateTime

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
        it.setGraph(R.navigation.holder_nav_graph)
        it.setCurrentDestination(R.id.nav_my_overview)
    }

    @Test
    fun `Recyclerview has MyOverviewHeaderAdapterItem and two MyOverviewNavigationCardAdapterItem when LocalTestResult does not exist`() {
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

    @Test
    fun `Recyclerview has MyOverviewHeaderAdapterItem, MyOverviewTestResultAdapterItem and two MyOverviewNavigationCardAdapterItem when LocalTestResult is valid`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.Valid(
                LocalTestResult(
                    credentials = "dummy",
                    sampleDate = OffsetDateTime.now(),
                    expireDate = OffsetDateTime.now(),
                    testType = "dummy"
                )
            )
        )
        assertListItemCount(
            listId = R.id.recyclerView,
            expectedItemCount = 4
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
            targetViewId = R.id.test_result_title,
            textId = R.string.my_overview_existing_qr_title
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 2,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_appointment_title
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 3,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_qr_title
        )
    }

    @Test
    fun `Recyclerview has MyOverviewHeaderAdapterItem, MyOverviewTestResultExpiredAdapterItem and two MyOverviewNavigationCardAdapterItem when LocalTestResult is expired`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.Expired
        )
        assertListItemCount(
            listId = R.id.recyclerView,
            expectedItemCount = 4
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
            targetViewId = R.id.text,
            text = ApplicationProvider.getApplicationContext<MainApplication>()
                .getString(R.string.item_test_result_expired).fromHtml().toStr()
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 2,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_appointment_title
        )
        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 3,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_qr_title
        )
    }

    @Test
    fun `Clicking close button in MyOverviewTestResultExpiredAdapterItem removes the view from RecyclerView`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.Expired
        )
        assertListItemCount(
            listId = R.id.recyclerView,
            expectedItemCount = 4
        )
        clickListItemChild(
            id = R.id.recyclerView,
            position = 1,
            childId = R.id.close
        )
        assertListItemCount(
            listId = R.id.recyclerView,
            expectedItemCount = 3
        )
    }

    @Test
    fun `Clicking qr code in MyOverviewTestResultAdapterItem navigates to new screen`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.Valid(
                LocalTestResult(
                    credentials = "dummy",
                    sampleDate = OffsetDateTime.now(),
                    expireDate = OffsetDateTime.now(),
                    testType = "dummy"
                )
            )
        )
        clickListItemChild(
            id = R.id.recyclerView,
            position = 1,
            childId = R.id.test_result_qr_image
        )
        assertEquals(navController.currentDestination?.id, R.id.nav_qr_code)
    }

    @Test
    fun `Clicking button in first MyOverviewNavigationCardAdapterItem navigates to new screen`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.None
        )
        clickListItemChild(
            id = R.id.recyclerView,
            position = 1,
            childId = R.id.button
        )
        assertEquals(navController.currentDestination?.id, R.id.nav_make_appointment)
    }

    @Test
    fun `Clicking button in second MyOverviewNavigationCardAdapterItem navigates to new screen`() {
        launchOverviewFragment(
            localTestResultState = LocalTestResultState.None
        )
        clickListItemChild(
            id = R.id.recyclerView,
            position = 2,
            childId = R.id.button
        )
        assertEquals(navController.currentDestination?.id, R.id.nav_choose_provider)
    }

    private fun launchOverviewFragment(localTestResultState: LocalTestResultState) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeLocalTestResultViewModel(
                        localTestResultState = localTestResultState
                    )
                }
                factory {
                    fakePersistenceManager(
                        secretKeyJson = ""
                    )
                }
                factory {
                    fakeQrCodeUseCase()
                }
            }
        )

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
