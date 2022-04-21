package nl.rijksoverheid.ctr.holder.dashboard.util

import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItem

import org.junit.Test

class DashboardPageAccessibilityUtilImplTest {
    
    val dashboardPageAccessibilityUtil = DashboardPageAccessibilityUtilImpl()

    @Test
    fun `0 new items to announce`() {
        val infoItem = mockk<DashboardInfoCardAdapterItem>(relaxed = true)
        val oldHeaderItem = mockk<DashboardHeaderAdapterItem>(relaxed = true)
        val newHeaderItem = mockk<DashboardHeaderAdapterItem>(relaxed = true)
        
        dashboardPageAccessibilityUtil.announceNewInfoItems(listOf(oldHeaderItem, infoItem), listOf(newHeaderItem))
        
        verify(exactly = 0) { infoItem.focusForAccessibility() }
    }

    @Test
    fun `1 new items to announce`() {
        val oldHeaderItem = mockk<DashboardHeaderAdapterItem>(relaxed = true)
        val newHeaderItem = mockk<DashboardHeaderAdapterItem>(relaxed = true)
        val newInfoItem = mockk<DashboardInfoCardAdapterItem>(relaxed = true)

        dashboardPageAccessibilityUtil.announceNewInfoItems(listOf(oldHeaderItem), listOf(newHeaderItem, newInfoItem))

        verify(exactly = 1) { newInfoItem.focusForAccessibility() }
    }
}