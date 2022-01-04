package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.ServerTime
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtil
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
import java.time.OffsetDateTime

@RunWith(RobolectricTestRunner::class)
class GetDashboardItemsUseCaseImplTest : AutoCloseKoinTest() {

    private val usecase: GetDashboardItemsUseCase by inject()

    @Before
    fun setup() {
        loadKoinModules(
            module(override = true) {
                factory {
                    mockk<FeatureFlagUseCase>().apply {
                        every { isVerificationPolicyEnabled() } answers { true }
                    }
                }
                factory {
                    mockk<BuildConfigUseCase>().apply {
                        every { getVersionCode() } answers { 2 }
                    }
                }
                factory {
                    mockk<CachedAppConfigUseCase>().apply {
                        every { getCachedAppConfig() } answers {
                            HolderConfig.default(
                                holderRecommendedVersion = 1
                            )
                        }
                    }
                }
            }
        )
    }

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
        val domesticGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination,
            eventTime = OffsetDateTime.now().minusHours(1),
            expirationTime = OffsetDateTime.now().plusHours(5),
            validFrom = OffsetDateTime.now().minusHours(5)
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
    fun `getItems returns correct models for single domestic and single international green card`() =
        runBlocking {
            loadKoinModules(fakeDashboardItemUtilModule())

            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
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
    fun `getItems returns correct models for domestic and international green cards`() =
        runBlocking {
            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
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
    fun `getItems returns correct models for domestic and international green cards with clock deviation`() =
        runBlocking {
            loadKoinModules(fakeClockDeviationModule(hasDeviation = true))

            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
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
        val domesticGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination,
            eventTime = OffsetDateTime.now().minusHours(1),
            expirationTime = OffsetDateTime.now().minusHours(5),
            validFrom = OffsetDateTime.now().minusHours(5)
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

    @Test
    fun `getItems returns 3g validity card when there is a test origin and 3g policy`() =
        runBlocking {
            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Test,
                category = "3"
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(domesticGreenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertTrue(dashboardItems.domesticItems.any { it is DashboardItem.InfoItem.TestCertificate3GValidity })
        }

    @Test
    fun `getItems gives no 3g validity card when there is no test origin and 3g policy`() =
        runBlocking {
            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Vaccination,
                category = "3"
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(domesticGreenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertTrue(dashboardItems.domesticItems.none { it is DashboardItem.InfoItem.TestCertificate3GValidity })
        }

    @Test
    fun `getItems gives no 3g validity card when there a test origin and 2g policy`() =
        runBlocking {
            val domesticGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Test,
                category = "2"
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(domesticGreenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertTrue(dashboardItems.domesticItems.none { it is DashboardItem.InfoItem.TestCertificate3GValidity })
        }

    @Test
    fun `getItems gives no 3g validity card for international green cards`() =
        runBlocking {
            val euGreenCard = fakeGreenCard(
                greenCardType = GreenCardType.Eu,
                originType = OriginType.Test,
                category = "3"
            )

            val dashboardItems = usecase.getItems(
                allGreenCards = listOf(euGreenCard),
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                isLoadingNewCredentials = false,
                allEventGroupEntities = listOf()
            )

            assertTrue(dashboardItems.domesticItems.none { it is DashboardItem.InfoItem.TestCertificate3GValidity })
        }

    fun `getItems returns app update card when update is available`() = runBlocking {
        loadKoinModules(fakeDashboardItemUtilModule(isAppUpdateAvailable = true))

        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        assertTrue(dashboardItems.domesticItems.any { it is DashboardItem.InfoItem.AppUpdate})
        assertTrue(dashboardItems.internationalItems.any { it is DashboardItem.InfoItem.AppUpdate})
    }

    private fun fakeClockDeviationModule(hasDeviation: Boolean) = module(override = true) {
        factory<ClockDeviationUseCase> {
            object : ClockDeviationUseCase() {
                override fun store(serverTime: ServerTime) {

                }

                override fun hasDeviation(): Boolean {
                    return hasDeviation
                }

                override fun calculateServerTimeOffsetMillis(): Long {
                    return 0L
                }
            }
        }
    }

    private fun fakeDashboardItemUtilModule(
        isAppUpdateAvailable: Boolean = false
    ) = module(override = true) {
        factory<DashboardItemUtil> {
            object : DashboardItemUtil {
                override fun getHeaderItemText(
                    greenCardType: GreenCardType,
                    allGreenCards: List<GreenCard>
                ): Int = R.string.my_overview_qr_placeholder_header

                override fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>) = false

                override fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>) = false

                override fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>) = false

                override fun isAppUpdateAvailable() = isAppUpdateAvailable

                override fun combineEuVaccinationItems(items: List<DashboardItem>) = listOf(
                    DashboardItem.CardsItem(
                        emptyList()
                    )
                )

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

                override fun shouldShowNewValidityItem(): Boolean {
                    return false
                }

                override fun shouldShowTestCertificate3GValidityItem(domesticGreenCards: List<GreenCard>): Boolean {
                    return false

                }
            }
        }
    }
}