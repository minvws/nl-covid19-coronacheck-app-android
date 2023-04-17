/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.fakeGreenCard
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem.CardsItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem.CardsItem.CardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemUtilImpl
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.dao.RemovedEventDao
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DashboardItemUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `shouldShowClockDeviationItem returns true if has deviation and no empty state`() {
        val clockDeviationUseCase: ClockDeviationUseCase = mockk()
        every { clockDeviationUseCase.hasDeviation() } answers { true }

        val util = getUtil(
            clockDeviationUseCase = clockDeviationUseCase
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard()),
            emptyState = false
        )

        assertEquals(true, shouldShowClockDeviationItem)
    }

    @Test
    fun `shouldShowClockDeviationItem returns false if has deviation and empty state`() {
        val clockDeviationUseCase: ClockDeviationUseCase = mockk()
        every { clockDeviationUseCase.hasDeviation() } answers { true }

        val util = getUtil(
            clockDeviationUseCase = clockDeviationUseCase
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard()),
            emptyState = true
        )

        assertEquals(false, shouldShowClockDeviationItem)
    }

    @Test
    fun `shouldShowPlaceholderItem returns true if empty state`() {
        val util = getUtil()

        val shouldShowPlaceholderItem = util.shouldShowPlaceholderItem(true)

        assertEquals(true, shouldShowPlaceholderItem)
    }

    @Test
    fun `shouldShowPlaceholderItem returns false if empty state`() {
        val util = getUtil()

        val shouldShowPlaceholderItem = util.shouldShowPlaceholderItem(false)

        assertEquals(false, shouldShowPlaceholderItem)
    }

    @Test
    fun `shouldAddQrButtonItem returns true if empty state`() {
        val util = getUtil()

        val shouldAddQrButtonItem = util.shouldAddQrButtonItem(
            emptyState = true
        )

        assertEquals(true, shouldAddQrButtonItem)
    }

    @Test
    fun `shouldAddQrButtonItem returns false if empty state`() {
        val util = getUtil()

        val shouldAddQrButtonItem = util.shouldAddQrButtonItem(
            emptyState = false
        )

        assertEquals(false, shouldAddQrButtonItem)
    }

    @Test
    fun `multiple vaccination card items should be combined into 1`() {
        val util = getUtil()

        val card1 = createCardItem(OriginType.Vaccination)
        val card2 = createCardItem(OriginType.Vaccination)
        val card3 = createCardItem(OriginType.Vaccination)

        val items = listOf(
            DashboardItem.HeaderItem(1, null),
            CardsItem(listOf(createCardItem(OriginType.Test))),
            CardsItem(listOf(card1)),
            CardsItem(listOf(createCardItem(OriginType.Recovery))),
            CardsItem(listOf(card2)),
            CardsItem(listOf(card3))
        )

        val combinedItems = util.combineEuVaccinationItems(items)

        // Total size of list smaller because of combined vaccination items
        assertTrue(combinedItems.size == 4)
        assertEquals((combinedItems[2] as CardsItem).cards[0], card1)
        assertEquals((combinedItems[2] as CardsItem).cards[1], card2)
        assertEquals((combinedItems[2] as CardsItem).cards[2], card3)
    }

    @Test
    fun `App update is available when the recommended version is higher than current version`() {
        val appConfigUseCase: HolderCachedAppConfigUseCase = mockk()
        every { appConfigUseCase.getCachedAppConfig().recommendedVersion } answers { 2 }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 1 }

        val util = getUtil(
            appConfigUseCase = appConfigUseCase,
            buildConfigUseCase = buildConfigUseCase
        )

        assertTrue(util.isAppUpdateAvailable())
    }

    @Test
    fun `App update is not available when the recommended version is current version`() {
        val appConfigUseCase: HolderCachedAppConfigUseCase = mockk()
        every { appConfigUseCase.getCachedAppConfig().recommendedVersion } answers { 1 }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 1 }

        val util = getUtil(
            appConfigUseCase = appConfigUseCase,
            buildConfigUseCase = buildConfigUseCase
        )

        assertFalse(util.isAppUpdateAvailable())
    }

    @Test
    fun `App update is not available when the recommended version lower is current version`() {
        val appConfigUseCase: HolderCachedAppConfigUseCase = mockk()
        every { appConfigUseCase.getCachedAppConfig().recommendedVersion } answers { 1 }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val util = getUtil(
            appConfigUseCase = appConfigUseCase,
            buildConfigUseCase = buildConfigUseCase
        )

        assertFalse(util.isAppUpdateAvailable())
    }

    @Test
    fun `shouldShowAddQrCardItem returns true if no empty state`() {
        val util = getUtil()

        val shouldShowAddQrItem = util.shouldShowAddQrCardItem(
            hasVisitorPassIncompleteItem = false,
            emptyState = false
        )

        assertTrue(shouldShowAddQrItem)
    }

    @Test
    fun `shouldShowAddQrCardItem returns false when empty state`() {
        val util = getUtil()

        val shouldShowAddQrItem = util.shouldShowAddQrCardItem(
            hasVisitorPassIncompleteItem = false,
            emptyState = true
        )

        assertFalse(shouldShowAddQrItem)
    }

    @Test
    fun `shouldShowBlockedEventsItem return false when no blocked events in database`() =
        runBlocking {
            val removedEventDao = mockk<RemovedEventDao>(relaxed = true)
            val holderDatabase = mockk<HolderDatabase>(relaxed = true).apply {
                coEvery { removedEventDao() } returns removedEventDao
            }
            coEvery { removedEventDao.getAll(reason = RemovedEventReason.Blocked) } answers { listOf() }

            val util = getUtil(
                holderDatabase = holderDatabase
            )

            assertFalse(util.shouldShowBlockedEventsItem())
        }

    private fun createCardItem(originType: OriginType) = CardItem(
        greenCard = GreenCard(
            greenCardEntity = nl.rijksoverheid.ctr.fakeGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    greenCardId = 1L,
                    type = originType,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(),
                    validFrom = OffsetDateTime.now()
                )
            ),
            credentialEntities = listOf()
        ),
        originStates = listOf(),
        credentialState = CardsItem.CredentialState.HasCredential(mockk()),
        databaseSyncerResult = mockk(),
        greenCardEnabledState = GreenCardEnabledState.Enabled
    )

    private fun getEvent(originType: OriginType) = EventGroupEntity(
        id = 0,
        walletId = 0,
        providerIdentifier = "1",
        type = originType,
        expiryDate = OffsetDateTime.now(),
        jsonData = "".toByteArray(),
        scope = "",
        draft = false
    )

    private fun getGreenCard(originType: OriginType) = GreenCard(
        greenCardEntity = mockk(),
        origins = listOf(
            OriginEntity(
                id = 0,
                greenCardId = 0,
                type = originType,
                eventTime = OffsetDateTime.now(),
                expirationTime = OffsetDateTime.now(),
                validFrom = OffsetDateTime.now()
            )
        ),
        credentialEntities = listOf()
    )

    private fun getUtil(
        clockDeviationUseCase: ClockDeviationUseCase = mockk(relaxed = true),
        appConfigFreshnessUseCase: AppConfigFreshnessUseCase = mockk(relaxed = true),
        appConfigUseCase: HolderCachedAppConfigUseCase = mockk(relaxed = true),
        buildConfigUseCase: BuildConfigUseCase = mockk(relaxed = true),
        holderDatabase: HolderDatabase = mockk(relaxed = true)
    ) = DashboardItemUtilImpl(
        clockDeviationUseCase = clockDeviationUseCase,
        appConfigFreshnessUseCase = appConfigFreshnessUseCase,
        appConfigUseCase = appConfigUseCase,
        buildConfigUseCase = buildConfigUseCase,
        holderDatabase = holderDatabase
    )
}
