package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import nl.rijksoverheid.ctr.holder.fakeClockDevationUseCase
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.fakeGreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardItemUtilImplTest {

    @Test
    fun `shouldShowHeaderItem returns true if has green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil()
        )

        val shouldShowHeaderItem = util.shouldShowHeaderItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldShowHeaderItem returns true if not all green cards expired`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            )
        )

        val shouldShowHeaderItem = util.shouldShowHeaderItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldShowClockDeviationItem returns true if has deviation and has green cards`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(
                hasDeviation = true
            ),
            greenCardUtil = fakeGreenCardUtil()
        )

        val shouldShowHeaderItem = util.shouldShowHeaderItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowHeaderItem)
    }

    @Test
    fun `shouldShowClockDeviationItem returns true if has deviation and not all green cards expired`() {
        val util = DashboardItemUtilImpl(
            clockDeviationUseCase = fakeClockDevationUseCase(
                hasDeviation = true
            ),
            greenCardUtil = fakeGreenCardUtil(
                isExpired = false
            )
        )

        val shouldShowHeaderItem = util.shouldShowClockDeviationItem(
            allGreenCards = listOf(fakeGreenCard)
        )

        assertEquals(true, shouldShowHeaderItem)
    }
}