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
            )
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
            greenCardUtil = fakeGreenCardUtil()
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
            )
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
            greenCardUtil = fakeGreenCardUtil()
        )

        val shouldAddQrButtonItem = util.shouldAddQrButtonItem(
            allGreenCards = listOf()
        )

        assertEquals(true, shouldAddQrButtonItem)
    }
}