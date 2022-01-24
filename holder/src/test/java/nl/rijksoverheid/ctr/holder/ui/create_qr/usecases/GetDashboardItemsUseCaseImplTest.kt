package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.fakeEventGroupEntity
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.OffsetDateTime

@RunWith(RobolectricTestRunner::class)
class GetDashboardItemsUseCaseImplTest : AutoCloseKoinTest() {

    private val usecase: GetDashboardItemsUseCase by inject()

    @Test
    fun `getItems returns correct models when no green cards`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf()
        )

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals(3, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals( 3, dashboardItems.internationalItems.size)
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

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals(5, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.BoosterItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.domesticItems[4] is DashboardItem.AddQrButtonItem)

        assertEquals(4, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.BoosterItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.InfoItem.OriginInfoItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
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

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals( 3, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.MissingDutchVaccinationItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(4, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for single domestic and single international green card`() = runBlocking {
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

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals(5, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.BoosterItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.CardsItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.CoronaMelderItem)
        assertTrue(dashboardItems.domesticItems[4] is DashboardItem.AddQrButtonItem)

        assertEquals( 5, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.BoosterItem)
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

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals(5, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.BoosterItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.InfoItem.DomesticVaccinationExpiredItem)
        assertTrue(dashboardItems.domesticItems[3] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[4] is DashboardItem.AddQrButtonItem)

        assertEquals( 4, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.BoosterItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
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

        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals(3, dashboardItems.domesticItems.size, )
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals(4, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.GreenCardExpiredItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[3] is DashboardItem.AddQrButtonItem)
    }

    @Test
    fun `getItems returns correct models for when invalid visitor pass banner is showing`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success(),
            isLoadingNewCredentials = false,
            allEventGroupEntities = listOf(
                fakeEventGroupEntity(type = OriginType.VaccinationAssessment)
            )
        )

        println("getItems returns correct models for when invalid visitor pass banner is showing")
        dashboardItems.domesticItems.map { it.javaClass }.forEach(::println)
        dashboardItems.internationalItems.map { it.javaClass }.forEach(::println)

        assertEquals( 3, dashboardItems.domesticItems.size)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.InfoItem.VisitorPassIncompleteItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)

        assertEquals( 3, dashboardItems.internationalItems.size)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.HeaderItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.InfoItem.OriginInfoItem)
        assertTrue(dashboardItems.internationalItems[2] is DashboardItem.AddQrButtonItem)
    }
}