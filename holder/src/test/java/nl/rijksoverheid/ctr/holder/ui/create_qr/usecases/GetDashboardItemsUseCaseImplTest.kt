package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.fakeCredentialUtil
import nl.rijksoverheid.ctr.holder.fakeDashboardItemUtil
import nl.rijksoverheid.ctr.holder.fakeGreenCardUtil
import nl.rijksoverheid.ctr.holder.fakeOriginUtil
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDashboardItemsUseCaseImplTest {

    @Test
    fun `getItems returns correct models when no green cards`() = runBlocking {
        val usecase = GetDashboardItemsUseCaseImpl(
            greenCardUtil = fakeGreenCardUtil(),
            credentialUtil = fakeCredentialUtil(),
            originUtil = fakeOriginUtil(),
            dashboardItemUtil = fakeDashboardItemUtil(
                shouldShowPlaceholderItem = true
            )
        )
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