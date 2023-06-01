/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.usecases

import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.usecases.GetDashboardItemsUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetDashboardItemsUseCaseImplTest : AutoCloseKoinTest() {

    private val usecase: GetDashboardItemsUseCase by inject()

    @Before
    fun setup() {
        loadKoinModules(module {
            factory {
                mockk<BuildConfigUseCase>(relaxed = true).apply {
                    every { getVersionCode() } returns 99999
                }
            }
        })
    }

    @Test
    fun `getItems returns correct models when no green cards`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(3, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for single international green card`() = runBlocking {
        val internationalGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination,
            eventTime = OffsetDateTime.now().minusHours(1),
            expirationTime = OffsetDateTime.now().plusHours(5),
            validFrom = OffsetDateTime.now().minusHours(5)
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(3, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrCardItem)
    }

    @Test
    fun `getItems returns correct models for single domestic and single international green card`() =
        runBlocking {
            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                eventTime = OffsetDateTime.now().minusHours(1),
                expirationTime = OffsetDateTime.now().plusHours(5),
                validFrom = OffsetDateTime.now().minusHours(5)
            )

            val internationalGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                eventTime = OffsetDateTime.now().minusHours(1),
                expirationTime = OffsetDateTime.now().plusHours(5),
                validFrom = OffsetDateTime.now().minusHours(5)
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(domesticGreenCard, internationalGreenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertEquals(3, dashboardItems.internationalItems.size)
            assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
            assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
            assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrCardItem)
        }

    @Test
    fun `getItems returns correct models for expired international green card`() = runBlocking {
        val internationalGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination,
            eventTime = OffsetDateTime.now().minusHours(1),
            expirationTime = OffsetDateTime.now().minusHours(5),
            validFrom = OffsetDateTime.now().minusHours(5)
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(3, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.GreenCardExpiredItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrCardItem)
    }

    @Test
    fun `getItems returns the add qr button item when there is an empty state`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertTrue(dashboardItems.internationalItems.contains(DashboardItem.AddQrButtonItem))
    }

    @Test
    fun `getItems returns add qr button card item when there is a green card`() =
        runBlocking {
            val greenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                eventTime = OffsetDateTime.now().minusHours(1),
                expirationTime = OffsetDateTime.now().plusHours(5),
                validFrom = OffsetDateTime.now().minusHours(5)
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(greenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertTrue(dashboardItems.internationalItems.contains(DashboardItem.AddQrCardItem))
        }
}
