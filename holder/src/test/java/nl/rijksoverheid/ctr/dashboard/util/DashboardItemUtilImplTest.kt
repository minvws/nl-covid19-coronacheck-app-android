/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem.CardsItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem.CardsItem.CardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.OffsetDateTime

@RunWith(RobolectricTestRunner::class)
class DashboardItemUtilImplTest : AutoCloseKoinTest() {

    private val greenCardUtil: GreenCardUtil by inject()

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
    fun `shouldShowMissingDutchVaccinationItem returns true if no nl vaccination card and there is a eu vaccination card`() {
        val util = getUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(),
            euGreenCards = listOf(fakeGreenCard(originType = OriginType.Vaccination)),
        )

        assertTrue(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowMissingDutchVaccinationItem returns false if there is a nl vaccination card`() {
        val util = getUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(fakeGreenCard(originType = OriginType.Vaccination)),
            euGreenCards = listOf(fakeEuropeanVaccinationGreenCard),
        )

        assertFalse(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowMissingDutchVaccinationItem returns false if there is no eu vaccination card`() {
        val util = getUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(),
            euGreenCards = listOf(),
        )

        assertFalse(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if no green cards`() {
        val util = getUtil()

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertFalse(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns true if green cards`() {
        val util = getUtil()

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeGreenCard()),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertTrue(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if green cards but expired`() {
        val greenCardUtil: GreenCardUtil = mockk()
        every { greenCardUtil.isExpired(any()) } answers { true }

        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeGreenCard()),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertFalse(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if green cards but with error DatabaseSyncerResult`() {
        val util = getUtil()

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeGreenCard()),
            databaseSyncerResult = DatabaseSyncerResult.Failed.Error(
                AppErrorResult(
                    HolderStep.TestResultNetworkRequest,
                    IllegalStateException()
                )
            )
        )

        assertFalse(shouldShowCoronaMelderItem)
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
    fun `shouldShowVisitorPassIncompleteItem returns true if has vaccination assessment event and no vaccination assessment origin`() {
        val util = getUtil()

        val shouldShowVisitorPassIncompleteItem = util.shouldShowVisitorPassIncompleteItem(
            events = listOf(getEvent(originType = OriginType.VaccinationAssessment)),
            domesticGreenCards = listOf()
        )

        assertTrue(shouldShowVisitorPassIncompleteItem)
    }

    @Test
    fun `shouldShowVisitorPassIncompleteItem returns false if has vaccination assessment event and vaccination assessment origin`() {
        val util = getUtil()

        val shouldShowVisitorPassIncompleteItem = util.shouldShowVisitorPassIncompleteItem(
            events = listOf(getEvent(originType = OriginType.VaccinationAssessment)),
            domesticGreenCards = listOf(getGreenCard(originType = OriginType.VaccinationAssessment))
        )

        assertFalse(shouldShowVisitorPassIncompleteItem)
    }

    @Test
    fun `shouldShowOriginInfoItem returns false if vaccination assessment green card exists and card is for domestic test`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowOriginInfoItem = util.shouldShowOriginInfoItem(
            greenCards = listOf(fakeGreenCard(originType = OriginType.VaccinationAssessment)),
            greenCardType = GreenCardType.Domestic,
            originInfoTypeOrigin = OriginType.Test,
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        assertFalse(shouldShowOriginInfoItem)
    }

    @Test
    fun `shouldShowOriginInfoItem returns true if no vaccination assessment green card exists and card is for domestic test`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowOriginInfoItem = util.shouldShowOriginInfoItem(
            greenCards = listOf(),
            greenCardType = GreenCardType.Domestic,
            originInfoTypeOrigin = OriginType.Test,
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        assertTrue(shouldShowOriginInfoItem)
    }

    @Test
    fun `shouldShowOriginInfoItem returns true if vaccination assessment green card exists and card is for domestic vaccination`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowOriginInfoItem = util.shouldShowOriginInfoItem(
            greenCards = listOf(fakeGreenCard(originType = OriginType.VaccinationAssessment)),
            greenCardType = GreenCardType.Domestic,
            originInfoTypeOrigin = OriginType.Vaccination,
            disclosurePolicy = DisclosurePolicy.ThreeG
        )

        assertTrue(shouldShowOriginInfoItem)
    }

    @Test
    fun `shouldShowOriginInfoItem returns false if 0G green card exists and card is for domestic vaccination`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowOriginInfoItem = util.shouldShowOriginInfoItem(
            greenCards = listOf(fakeGreenCard(originType = OriginType.VaccinationAssessment)),
            greenCardType = GreenCardType.Domestic,
            originInfoTypeOrigin = OriginType.Vaccination,
            disclosurePolicy = DisclosurePolicy.ZeroG
        )

        assertFalse(shouldShowOriginInfoItem)
    }

    @Test
    fun `shouldShowAddQrCardItem returns true if no empty state`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowAddQrItem = util.shouldShowAddQrCardItem(
            hasVisitorPassIncompleteItem = false,
            emptyState = false
        )

        assertTrue(shouldShowAddQrItem)
    }

    @Test
    fun `shouldShowAddQrCardItem returns false when empty state`() {
        val util = getUtil(
            greenCardUtil = greenCardUtil
        )

        val shouldShowAddQrItem = util.shouldShowAddQrCardItem(
            hasVisitorPassIncompleteItem = false,
            emptyState = true
        )

        assertFalse(shouldShowAddQrItem)
    }

    @Test
    fun `showPolicyInfoItem returns true when it's not the same as the one dismissed`() {
        val util = getUtil(
            persistenceManager = mockk {
                every { getPolicyBannerDismissed() } returns DisclosurePolicy.OneG
            }
        )

        assertEquals(true, util.shouldShowPolicyInfoItem(
            disclosurePolicy = DisclosurePolicy.ThreeG,
            tabType = GreenCardType.Domestic
        ))
    }

    @Test
    fun `showPolicyInfoItem returns false when the config policy is the same as the one dismissed`() {
        val util = getUtil(
            persistenceManager = mockk {
                every { getPolicyBannerDismissed() } returns DisclosurePolicy.ThreeG
            }
        )

        assertEquals(false, util.shouldShowPolicyInfoItem(
            disclosurePolicy = DisclosurePolicy.ThreeG,
            tabType = GreenCardType.Domestic
        ))
    }

    @Test
    fun `showPolicyInfoItem returns false when the config policy is 0G and domestic tab is selected`() {
        val util = getUtil(
            persistenceManager = mockk {
                every { getPolicyBannerDismissed() } returns DisclosurePolicy.ThreeG
            }
        )

        assertEquals(false, util.shouldShowPolicyInfoItem(
            disclosurePolicy = DisclosurePolicy.ZeroG,
            tabType = GreenCardType.Domestic
        ))
    }

    @Test
    fun `showPolicyInfoItem returns true when the config policy is 0G and eu tab is selected`() {
        val util = getUtil(
            persistenceManager = mockk {
                every { getPolicyBannerDismissed() } returns DisclosurePolicy.ThreeG
            }
        )

        assertEquals(true, util.shouldShowPolicyInfoItem(
            disclosurePolicy = DisclosurePolicy.ZeroG,
            tabType = GreenCardType.Eu
        ))
    }

    private fun createCardItem(originType: OriginType) = CardItem(
        greenCard = GreenCard(
            greenCardEntity = fakeGreenCardEntity,
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
        disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
        greenCardEnabledState = GreenCardEnabledState.Enabled
    )

    private fun getEvent(originType: OriginType) = EventGroupEntity(
        id = 0,
        walletId = 0,
        providerIdentifier = "1",
        type = originType,
        maxIssuedAt = OffsetDateTime.now(),
        jsonData = "".toByteArray(),
        scope = ""
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
        persistenceManager: PersistenceManager = mockk(relaxed = true),
        appConfigFreshnessUseCase: AppConfigFreshnessUseCase = mockk(relaxed = true),
        appConfigUseCase: HolderCachedAppConfigUseCase = mockk(relaxed = true),
        buildConfigUseCase: BuildConfigUseCase = mockk(relaxed = true),
        greenCardUtil: GreenCardUtil = mockk(relaxed = true),
    ) = DashboardItemUtilImpl(
        clockDeviationUseCase = clockDeviationUseCase,
        persistenceManager = persistenceManager,
        appConfigFreshnessUseCase = appConfigFreshnessUseCase,
        appConfigUseCase = appConfigUseCase,
        buildConfigUseCase = buildConfigUseCase,
        greenCardUtil = greenCardUtil
    )
}