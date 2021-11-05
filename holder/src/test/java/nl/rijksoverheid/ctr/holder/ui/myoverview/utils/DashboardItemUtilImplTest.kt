package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.GreenCardExpiredItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.HeaderItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Domestic,
            allGreenCards = listOf(fakeGreenCard)
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Domestic,
            allGreenCards = listOf()
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Eu,
            allGreenCards = listOf(fakeGreenCard)
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val headerText = util.getHeaderItemText(
            greenCardType = GreenCardType.Eu,
            allGreenCards = listOf()
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard)
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldShowClockDeviationItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowClockDeviationItem)
    }

    @Test
    fun `shouldShowPlaceholderItem returns true if has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldShowHeaderItem = util.shouldShowPlaceholderItem(
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
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldShowHeaderItem = util.shouldShowPlaceholderItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldAddQrButtonItem returns true if has no green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddQrButtonItem = util.shouldAddQrButtonItem(
            allGreenCards = listOf()
        )

        assertEquals(true, shouldAddQrButtonItem)
    }

    @Test
    fun `multiple vaccination card items should be combined into 1`() {
        val util = DashboardItemUtilImpl(mockk(), mockk(), mockk(), mockk(),mockk())

        val card1 = createCardItem(OriginType.Vaccination)
        val card2 = createCardItem(OriginType.Vaccination)
        val card3 = createCardItem(OriginType.Vaccination)

        val items = listOf(
            HeaderItem(1),
            CardsItem(listOf(createCardItem(OriginType.Test))),
            CardsItem(listOf(card1)),
            CardsItem(listOf(createCardItem(OriginType.Recovery))),
            CardsItem(listOf(card2)),
            GreenCardExpiredItem(mockk()),
            CardsItem(listOf(card3))
        )

        val combinedItems = util.combineEuVaccinationItems(items)

        // Total size of list smaller because of combined vaccination items
        assertTrue(combinedItems.size == 5)
        assertEquals((combinedItems[2] as CardsItem).cards[0], card1)
        assertEquals((combinedItems[2] as CardsItem).cards[1], card2)
        assertEquals((combinedItems[2] as CardsItem).cards[2], card3)
    }

    @Test
    fun `shouldAddSyncGreenCardsItem returns false if no vaccination events`() = runBlocking {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(
                remoteEventVaccinations = listOf()
            ),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddSyncGreenCardsItem = util.shouldAddSyncGreenCardsItem(
            allGreenCards = listOf(),
            allEventGroupEntities = listOf()
        )

        assertEquals(false, shouldAddSyncGreenCardsItem)
    }

    @Test
    fun `shouldAddSyncGreenCardsItem returns false if there is a single vaccination event`() = runBlocking {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(
                remoteEventVaccinations = listOf(
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination()
                    )
                )
            ),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddSyncGreenCardsItem = util.shouldAddSyncGreenCardsItem(
            allGreenCards = listOf(),
            allEventGroupEntities = listOf()
        )

        assertEquals(false, shouldAddSyncGreenCardsItem)
    }

    @Test
    fun `shouldAddSyncGreenCardsItem returns true if there are multiple vaccination events and one european green card`() = runBlocking {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(
                remoteEventVaccinations = listOf(
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination()
                    ),
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination()
                    )
                )
            ),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddSyncGreenCardsItem = util.shouldAddSyncGreenCardsItem(
            allGreenCards = listOf(fakeEuropeanVaccinationGreenCard),
            allEventGroupEntities = listOf()
        )

        assertEquals(true, shouldAddSyncGreenCardsItem)
    }

    @Test
    fun `shouldAddSyncGreenCardsItem returns false if there are multiple vaccination events and two european green card`() = runBlocking {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(),
            persistenceManager = fakePersistenceManager(),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(
                remoteEventVaccinations = listOf(
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination()
                    ),
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination()
                    )
                )
            ),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddSyncGreenCardsItem = util.shouldAddSyncGreenCardsItem(
            allGreenCards = listOf(fakeEuropeanVaccinationGreenCard, fakeEuropeanVaccinationGreenCard),
            allEventGroupEntities = listOf()
        )

        assertEquals(false, shouldAddSyncGreenCardsItem)
    }

    @Test
    fun `shouldAddGreenCardsSyncedItem returns false if multiple eu vaccinations and local flag set to true`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = true
            ),
            persistenceManager = fakePersistenceManager(
                hasDismissedUnsecureDeviceDialog = true
            ),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddGreenCardsSyncedItem = util.shouldAddGreenCardsSyncedItem(
            allGreenCards = listOf(fakeEuropeanVaccinationGreenCard, fakeEuropeanVaccinationGreenCard)
        )

        assertEquals(false, shouldAddGreenCardsSyncedItem)
    }

    @Test
    fun `shouldAddGreenCardsSyncedItem returns true if multiple eu vaccinations and local flag set to false`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = true
            ),
            persistenceManager = fakePersistenceManager(
                hasDismissedUnsecureDeviceDialog = false
            ),
            eventGroupEntityUtil = fakeEventGroupEntityUtil(),
            appConfigFreshnessUseCase = fakeAppConfigFreshnessUseCase()
        )

        val shouldAddGreenCardsSyncedItem = util.shouldAddGreenCardsSyncedItem(
            allGreenCards = listOf(fakeEuropeanVaccinationGreenCard, fakeEuropeanVaccinationGreenCard)
        )

        assertEquals(true, shouldAddGreenCardsSyncedItem)
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
            credentialEntities = emptyList()
        ),
        originStates = listOf(),
        credentialState = CardsItem.CredentialState.HasCredential(mockk()),
        databaseSyncerResult = mockk()
    )
}