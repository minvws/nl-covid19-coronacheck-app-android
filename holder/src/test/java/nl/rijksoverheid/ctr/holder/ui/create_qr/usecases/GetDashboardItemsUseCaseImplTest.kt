package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetDashboardItemsUseCaseImplTest: AutoCloseKoinTest() {

    private val usecase: GetDashboardItemsUseCase by inject()

    @Test
    fun `getItems returns correct models when no green cards`() = runBlocking {
        val dashboardItems = usecase.getItems(
            allGreenCards = listOf(),
            databaseSyncerResult = DatabaseSyncerResult.Success,
            isLoadingNewCredentials = false
        )

        assertEquals(dashboardItems.domesticItems.size, 2)
        assertTrue(dashboardItems.domesticItems[0] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.domesticItems[1] is DashboardItem.AddQrButtonItem)

        assertEquals(dashboardItems.internationalItems.size, 2)
        assertTrue(dashboardItems.internationalItems[0] is DashboardItem.PlaceholderCardItem)
        assertTrue(dashboardItems.internationalItems[1] is DashboardItem.AddQrButtonItem)
    }
}