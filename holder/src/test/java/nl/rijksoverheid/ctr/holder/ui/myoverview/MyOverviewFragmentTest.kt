package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonth
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeMyOverviewModel
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
        it.setCurrentDestination(R.id.nav_my_overview)
    }

    @Test
    fun `HeaderItem maps to MyOverviewHeaderAdapterItem in UI`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Domestic,
                items = listOf(
                    MyOverviewItem.HeaderItem(
                        text = R.string.ok
                    )
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.text,
            textId = R.string.ok
        )
    }

    @Test
    fun `CreateQrCardItem with no green cards maps to correct MyOverviewNavigationCardAdapterItem in UI`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Domestic,
                items = listOf(
                    MyOverviewItem.CreateQrCardItem(
                        hasGreenCards = false
                    )
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_make_qr_title
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.button,
            textId = R.string.my_overview_no_qr_make_qr_button
        )
    }

    @Test
    fun `CreateQrCardItem with green cards maps to correct MyOverviewNavigationCardAdapterItem in UI`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Domestic,
                items = listOf(
                    MyOverviewItem.CreateQrCardItem(
                        hasGreenCards = true
                    )
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.title,
            textId = R.string.my_overview_no_qr_replace_qr_title
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.button,
            textId = R.string.my_overview_no_qr_replace_qr_button
        )
    }

    @Test
    fun `GreenCardItem with type domestic maps to correct MyOverviewGreenCardAdapterItem in UI`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Domestic,
                items = getGreenCardItems(
                    type = GreenCardType.Domestic
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof1,
            text = InstrumentationRegistry.getInstrumentation().context.getString(
                R.string.qr_card_test_validity_domestic,
                OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ).formatDateTime(InstrumentationRegistry.getInstrumentation().context)
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof2,
            text = InstrumentationRegistry.getInstrumentation().context.getString(
                R.string.qr_card_vaccination_validity_domestic,
                OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ).toLocalDate().formatDayMonthYear()
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof3,
            text = InstrumentationRegistry.getInstrumentation().context.getString(
                R.string.qr_card_recovery_validity_domestic,
                OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ).toLocalDate().formatDayMonth()
            )
        )
    }

    @Test
    fun `GreenCardItem with type eu maps to correct MyOverviewGreenCardAdapterItem in UI`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Eu,
                items = getGreenCardItems(
                    type = GreenCardType.Eu
                )
            )
        )

        assertDisplayedAtPosition(
            listId = R.id.recyclerView,
            position = 0,
            targetViewId = R.id.proof1,
            text = InstrumentationRegistry.getInstrumentation().context.getString(
                R.string.qr_card_test_validity_eu,
                OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ).formatDateTime(InstrumentationRegistry.getInstrumentation().context)
            )
        )
    }

    @Test
    fun `If ToggleGreenCardTypeItem exists show travel mode`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Eu,
                items = listOf(MyOverviewItem.TravelModeItem)
            )
        )

        assertDisplayed(R.id.description)
    }

    @Test
    fun `If ToggleGreenCardTypeItem does not exist hide travel mode`() {
        launchOverviewFragment(
            items = MyOverviewItems(
                type = GreenCardType.Eu,
                items = listOf()
            )
        )

        assertNotDisplayed(R.id.description)
    }

    private fun getGreenCardItems(type: GreenCardType): List<MyOverviewItem.GreenCardItem> {
        val origins = listOf(
            OriginEntity(
                id = 1,
                greenCardId = 1,
                type = OriginType.Test,
                eventTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ),
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                )
            ),
            OriginEntity(
                id = 1,
                greenCardId = 1,
                type = OriginType.Vaccination,
                eventTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ),
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                )
            ),
            OriginEntity(
                id = 1,
                greenCardId = 1,
                type = OriginType.Recovery,
                eventTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                ),
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(1),
                    ZoneOffset.UTC
                )
            )
        )

        return listOf(
            MyOverviewItem.GreenCardItem(
                greenCard = GreenCard(
                    greenCardEntity = GreenCardEntity(
                        id = 1,
                        walletId = 1,
                        type = type
                    ),
                    origins = origins,
                    credentialEntities = listOf(),
                ),
                sortedOrigins = origins
            )
        )
    }

    private fun launchOverviewFragment(items: MyOverviewItems) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeMyOverviewModel(
                        items = items
                    )
                }
            }
        )

        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            MyOverviewFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
