package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.HeaderItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtilImpl
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.IllegalStateException
import java.time.OffsetDateTime
import kotlin.test.assertTrue

class DashboardItemUtilImplTest {

    @Test
    fun `getHeaderItemText returns correct text if domestic and has green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Domestic,
            allGreenCards = listOf(fakeGreenCard()),
            allEvents = listOf()
        )

        assertEquals(R.string.my_overview_description, headerText)
    }

    @Test
    fun `getHeaderItemText returns correct text if domestic and has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Domestic,
            allGreenCards = listOf(),
            allEvents = listOf()
        )

        assertEquals(R.string.my_overview_qr_placeholder_description, headerText)
    }

    @Test
    fun `getHeaderItemText returns correct text if eu and has green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Eu,
            allGreenCards = listOf(fakeGreenCard()),
            allEvents = listOf()
        )

        assertEquals(R.string.my_overview_description_eu, headerText)
    }

    @Test
    fun `getHeaderItemText returns correct text if eu and has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Eu,
            allGreenCards = listOf(),
            allEvents = listOf()
        )

        assertEquals(R.string.my_overview_qr_placeholder_description_eu, headerText)
    }

    @Test
    fun `shouldShowClockDeviationItem returns true if has deviation and has green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(
                hasDeviation = true
            ),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard())
        )

        assertEquals(true, shouldShowClockDeviationItem)
    }

    @Test
    fun `shouldShowClockDeviationItem returns true if has deviation and not all green cards expired`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(
                hasDeviation = true
            ),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard())
        )

        assertEquals(true, shouldShowClockDeviationItem)
    }

    @Test
    fun `shouldShowPlaceholderItem returns true if has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val shouldShowHeaderItem = util.shouldShowPlaceholderItem(
            allEvents = listOf(),
            allGreenCards = listOf()
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldShowPlaceholderItem returns true if all green cards expired`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = true
            ),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val shouldShowHeaderItem = util.shouldShowPlaceholderItem(
            allEvents = listOf(),
            allGreenCards = listOf(fakeGreenCard())
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldAddQrButtonItem returns true if has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
            featureFlagUseCase = mockk(),
            appConfigUseCase = mockk(),
            buildConfigUseCase = mockk()
        )

        val shouldAddQrButtonItem = util.shouldAddQrButtonItem(
            allEvents = listOf(),
            allGreenCards = listOf()
        )

        assertEquals(true, shouldAddQrButtonItem)
    }

    @Test
    fun `multiple vaccination card items should be combined into 1`() {

        val util = DashboardItemUtilImpl(mockk(), mockk(), mockk(), mockk(),mockk(), mockk(), mockk())

        val card1 = createCardItem(OriginType.Vaccination)
        val card2 = createCardItem(OriginType.Vaccination)
        val card3 = createCardItem(OriginType.Vaccination)

        val items = listOf(
            HeaderItem(1),
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
        val util = dashboardItemUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(fakeDomesticTestGreenCard),
            euGreenCards = listOf(fakeEuropeanVaccinationGreenCard),
        )

        assertTrue(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowMissingDutchVaccinationItem returns false if there is a nl vaccination card`() {
        val util = dashboardItemUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(fakeDomesticVaccinationGreenCard),
            euGreenCards = listOf(fakeEuropeanVaccinationGreenCard),
        )

        assertFalse(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowMissingDutchVaccinationItem returns false if there is no eu vaccination card`() {
        val util = dashboardItemUtil()

        val shouldShowMissingDutchVaccinationItem = util.shouldShowMissingDutchVaccinationItem(
            domesticGreenCards = listOf(fakeDomesticTestGreenCard),
            euGreenCards = listOf(fakeEuropeanVaccinationTestCard),
        )

        assertFalse(shouldShowMissingDutchVaccinationItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if there are no green cards`() {
        val util = dashboardItemUtil()

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertFalse(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if all green cards expired`() {
        val util = dashboardItemUtil(
            isExpired = true
        )

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeDomesticVaccinationGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertFalse(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if green cards but with error DatabaseSyncerResult`() {
        val util = dashboardItemUtil(
            isExpired = false
        )

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeDomesticVaccinationGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Failed.Error(AppErrorResult(HolderStep.GetCredentialsNetworkRequest, IllegalStateException()))
        )

        assertFalse(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowCoronaMelderItem returns true if green cards`() {
        val util = dashboardItemUtil(
            isExpired = false
        )

        val shouldShowCoronaMelderItem = util.shouldShowCoronaMelderItem(
            greenCards = listOf(fakeDomesticVaccinationGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success()
        )

        assertTrue(shouldShowCoronaMelderItem)
    }

    @Test
    fun `shouldShowTestCertificate3GValidityItem returns false if test has 2g category`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { true }

        val util = dashboardItemUtil(
            isExpired = false,
            featureFlagUseCase = featureFlagUseCase
        )

        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test,
            category = "2"
        )

        val shouldShowTestCertificate3GValidityItem = util.shouldShowTestCertificate3GValidityItem(
            domesticGreenCards = listOf(greenCard)
        )

        assertFalse(shouldShowTestCertificate3GValidityItem)
    }

    @Test
    fun `shouldShowTestCertificate3GValidityItem returns true if test has 3g category`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { true }

        val util = dashboardItemUtil(
            isExpired = false,
            featureFlagUseCase = featureFlagUseCase
        )

        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test,
            category = "3"
        )

        val shouldShowTestCertificate3GValidityItem = util.shouldShowTestCertificate3GValidityItem(
            domesticGreenCards = listOf(greenCard)
        )

        assertTrue(shouldShowTestCertificate3GValidityItem)
    }

    @Test
    fun `shouldShowTestCertificate3GValidityItem returns false if test has 3g category and feature disabled`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val util = dashboardItemUtil(
            isExpired = false,
            featureFlagUseCase = featureFlagUseCase
        )

        val greenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test,
            category = "3"
        )

        val shouldShowTestCertificate3GValidityItem = util.shouldShowTestCertificate3GValidityItem(
            domesticGreenCards = listOf(greenCard)
        )

        assertFalse(shouldShowTestCertificate3GValidityItem)
    }

    @Test
    fun `App update is available when the recommended version is higher than current version`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 1 }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), mockk(), mockk(),
            appConfigUseCase = mockk { every { getCachedAppConfig().recommendedVersion } returns 2 },
            featureFlagUseCase = featureFlagUseCase,
            buildConfigUseCase = buildConfigUseCase
        )

        assertTrue(util.isAppUpdateAvailable())
    }

    @Test
    fun `App update is not available when the recommended version is current version`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 1 }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), mockk(), mockk(),
            appConfigUseCase = mockk { every { getCachedAppConfig().recommendedVersion } returns 1 },
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        assertFalse(util.isAppUpdateAvailable())
    }

    @Test
    fun `App update is not available when the recommended version lower is current version`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), mockk(), mockk(),
            appConfigUseCase = mockk { every { getCachedAppConfig().recommendedVersion } returns 1 },
            featureFlagUseCase = featureFlagUseCase,
            buildConfigUseCase = buildConfigUseCase,
        )

        assertFalse(util.isAppUpdateAvailable())
    }

    @Test
    fun `shouldShowNewValidityItem returns true if banner needs to be shown`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        val persistenceManager = mockk<PersistenceManager>()
        every { cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard } answers { true }
        every { persistenceManager.getHasDismissedNewValidityInfoCard() } answers { false }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), persistenceManager = persistenceManager, mockk(),
            appConfigUseCase = cachedAppConfigUseCase,
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        assertTrue(util.shouldShowNewValidityItem())
    }

    @Test
    fun `shouldShowNewValidityItem returns false if feature not live yet`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        val persistenceManager = mockk<PersistenceManager>()
        every { cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard } answers { false }
        every { persistenceManager.getHasDismissedNewValidityInfoCard() } answers { false }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), persistenceManager = persistenceManager, mockk(),
            appConfigUseCase = cachedAppConfigUseCase,
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        assertFalse(util.shouldShowNewValidityItem())
    }

    @Test
    fun `shouldShowNewValidityItem returns false if banner does not need to show`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        val persistenceManager = mockk<PersistenceManager>()
        every { cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard } answers { true }
        every { persistenceManager.getHasDismissedNewValidityInfoCard() } answers { true }

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), persistenceManager = persistenceManager, mockk(),
            appConfigUseCase = cachedAppConfigUseCase,
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        assertFalse(util.shouldShowNewValidityItem())
    }

    @Test
    fun `shouldShowVisitorPassIncompleteItem returns true if has vaccination assessment event and no vaccination assessment origin`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        val persistenceManager = mockk<PersistenceManager>()
        every { cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard } answers { true }
        every { persistenceManager.getHasDismissedNewValidityInfoCard() } answers { true }

        val event = getEvent(
            originType = OriginType.VaccinationAssessment
        )

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), persistenceManager = persistenceManager, mockk(),
            appConfigUseCase = cachedAppConfigUseCase,
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        val shouldShowVisitorPassIncompleteItem = util.shouldShowVisitorPassIncompleteItem(
            events = listOf(event),
            domesticGreenCards = listOf()
        )

        assertTrue(shouldShowVisitorPassIncompleteItem)
    }

    @Test
    fun `shouldShowVisitorPassIncompleteItem returns false if has vaccination assessment event and vaccination assessment origin`() {
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { false }

        val buildConfigUseCase: BuildConfigUseCase = mockk()
        every { buildConfigUseCase.getVersionCode() } answers { 2 }

        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        val persistenceManager = mockk<PersistenceManager>()
        every { cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard } answers { true }
        every { persistenceManager.getHasDismissedNewValidityInfoCard() } answers { true }

        val event = getEvent(
            originType = OriginType.VaccinationAssessment
        )

        val greenCard = getGreenCard(
            originType = OriginType.VaccinationAssessment
        )

        val util = DashboardItemUtilImpl(
            mockk(), mockk(), persistenceManager = persistenceManager, mockk(),
            appConfigUseCase = cachedAppConfigUseCase,
            buildConfigUseCase = buildConfigUseCase,
            featureFlagUseCase = featureFlagUseCase
        )

        val shouldShowVisitorPassIncompleteItem = util.shouldShowVisitorPassIncompleteItem(
            events = listOf(event),
            domesticGreenCards = listOf(greenCard)
        )

        assertFalse(shouldShowVisitorPassIncompleteItem)
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
        databaseSyncerResult = mockk()
    )

    private fun dashboardItemUtil(
        isExpired: Boolean = false,
        featureFlagUseCase: FeatureFlagUseCase = mockk()) = DashboardItemUtilImpl(
        clockDeviationUseCase = fakeClockDevationUseCase(),
        greenCardUtil = fakeGreenCardUtil(
            isExpired = isExpired
        ),
        persistenceManager = fakePersistenceManager(
            hasDismissedUnsecureDeviceDialog = false
        ),
        appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase(),
        featureFlagUseCase = featureFlagUseCase,
        appConfigUseCase = mockk(),
        buildConfigUseCase = mockk()
    )

    private fun getEvent(originType: OriginType) = EventGroupEntity(
        id = 0,
        walletId = 0,
        providerIdentifier = "1",
        type = originType,
        maxIssuedAt = OffsetDateTime.now(),
        jsonData = "".toByteArray()
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
}