package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.OffsetDateTime

@RunWith(RobolectricTestRunner::class)
class GetDashboardItemsUseCaseImplTest: AutoCloseKoinTest() {

    private val usecase: GetDashboardItemsUseCase by inject()

    @Test
    fun `getItems returns correct models when no green cards`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 3)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 3)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for single domestic green card`() = runBlocking {
        val domesticGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val domesticGreenCard = GreenCard(
            greenCardEntity = domesticGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(domesticGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 4)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 3)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.OriginInfoItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for single international green card`() = runBlocking {
        loadKoinModules(fakeDashboardItemUtilModule())

        val internationalGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Eu
        )

        val internationalGreenCard = GreenCard(
            greenCardEntity = internationalGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 3)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.MissingDutchVaccinationItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 3)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for single domestic and single international green card`() = runBlocking {
        loadKoinModules(fakeDashboardItemUtilModule())

        val domesticGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val domesticGreenCard = GreenCard(
            greenCardEntity = domesticGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val internationalGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Eu
        )

        val internationalGreenCard = GreenCard(
            greenCardEntity = internationalGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(domesticGreenCard, internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 3)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 3)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for domestic and international green cards`() = runBlocking {
        val domesticGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val domesticGreenCard = GreenCard(
            greenCardEntity = domesticGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val internationalGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Eu
        )

        val internationalGreenCard = GreenCard(
            greenCardEntity = internationalGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(domesticGreenCard, internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 4)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 4)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for domestic and international green cards with clock deviation`() = runBlocking {
        loadKoinModules(fakeClockDeviationModule(hasDeviation = true))

        val domesticGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val domesticGreenCard = GreenCard(
            greenCardEntity = domesticGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val internationalGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Eu
        )

        val internationalGreenCard = GreenCard(
            greenCardEntity = internationalGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().plusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(domesticGreenCard, internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 5)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.ClockDeviationItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.domesticItems[4] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 5)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.ClockDeviationItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.internationalItems[4] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for expired domestic green card`() = runBlocking {
        val domesticGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val domesticGreenCard = GreenCard(
            greenCardEntity = domesticGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().minusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(domesticGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 4)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.GreenCardExpiredItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 3)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for expired international green card`() = runBlocking {
        val internationalGreenCardEntity = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Eu
        )

        val internationalGreenCard = GreenCard(
            greenCardEntity = internationalGreenCardEntity,
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = OffsetDateTime.now().minusHours(1),
                    expirationTime = OffsetDateTime.now().minusHours(5),
                    validFrom = OffsetDateTime.now().minusHours(5)
                )
            ),
            credentialEntities = listOf()
        )

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(internationalGreenCard),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertEquals(dashboardItems.domesticItems.size, 3)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 4)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.GreenCardExpiredItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
    }

    private fun fakeClockDeviationModule(hasDeviation: Boolean) = module(override = true) {
        factory<ClockDeviationUseCase> {
            object: ClockDeviationUseCase() {
                override fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long) {

                }

                override fun hasDeviation(): Boolean {
                    return hasDeviation
                }

                override fun getAdjustedClock(clock: Clock): Clock = Clock.systemDefaultZone()
            }
        }
    }

    private fun fakeDashboardItemUtilModule() = module(override = true) {
        factory<DashboardItemUtil> {
            object: DashboardItemUtil {
                override fun getHeaderItemText(
                    greenCardType: GreenCardType,
                    allGreenCards: List<GreenCard>
                ): Int = R.string.my_overview_qr_placeholder_header

                override fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>) = false

                override fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>) = false

                override fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>) = false

                override fun combineEuVaccinationItems(items: List<DashboardItem>) = listOf(DashboardItem.CardsItem(
                    emptyList()))

                override suspend fun shouldAddSyncGreenCardsItem(
                    allEventGroupEntities: List<EventGroupEntity>,
                    allGreenCards: List<GreenCard>
                ) = false

                override fun shouldAddGreenCardsSyncedItem(allGreenCards: List<GreenCard>) = false

                override fun shouldShowExtendDomesticRecoveryItem() = false

                override fun shouldShowRecoverDomesticRecoveryItem() = false

                override fun shouldShowExtendedDomesticRecoveryItem() = false

                override fun shouldShowRecoveredDomesticRecoveryItem() = false

                override fun shouldShowConfigFreshnessWarning() = false

                override fun getConfigFreshnessMaxValidity() = OffsetDateTime.now().toEpochSecond()

                override fun shouldShowMissingDutchVaccinationItem(
                    domesticGreenCards: List<GreenCard>,
                    euGreenCards: List<GreenCard>
                ) = true

                override fun shouldShowCoronaMelderItem(
                    greenCards: List<GreenCard>,
                    databaseSyncerResult: DatabaseSyncerResult
                ): Boolean = false
            }
        }
    }
}