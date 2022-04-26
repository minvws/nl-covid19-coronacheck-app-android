package nl.rijksoverheid.ctr.holder.dashboard.usecases

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import org.junit.Assert.*

import org.junit.Test

class CoronaMelderUseCaseImplTest {

    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase = mockk()
    private val greenCardUtil: GreenCardUtil = mockk()
    private val greenCard: GreenCard = mockk(relaxed = true)

    @Test
    fun `show coronamelder item if config says so and there are valid greencards`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().shouldShowCoronaMelderRecommendation } returns true
        every { greenCardUtil.isExpired(any()) } returns false

        val coronaMelderUseCase = ShowCoronaMelderItemUseCaseImpl(cachedAppConfigUseCase, greenCardUtil)

        assertTrue(coronaMelderUseCase.shouldShowCoronaMelderItem(listOf(greenCard), DatabaseSyncerResult.Success()))
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if no green cards`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().shouldShowCoronaMelderRecommendation } returns true
        every { greenCardUtil.isExpired(any()) } returns false

        val coronaMelderUseCase = ShowCoronaMelderItemUseCaseImpl(cachedAppConfigUseCase, greenCardUtil)

        assertFalse(coronaMelderUseCase.shouldShowCoronaMelderItem(emptyList(), DatabaseSyncerResult.Success()))
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if green cards but expired`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().shouldShowCoronaMelderRecommendation } returns true
        every { greenCardUtil.isExpired(any()) } returns true

        val coronaMelderUseCase = ShowCoronaMelderItemUseCaseImpl(cachedAppConfigUseCase, greenCardUtil)

        assertFalse(coronaMelderUseCase.shouldShowCoronaMelderItem(emptyList(), DatabaseSyncerResult.Success()))
    }

    @Test
    fun `shouldShowCoronaMelderItem returns false if green cards but with error DatabaseSyncerResult`() {
        every { cachedAppConfigUseCase.getCachedAppConfig().shouldShowCoronaMelderRecommendation } returns true
        every { greenCardUtil.isExpired(any()) } returns false

        val coronaMelderUseCase = ShowCoronaMelderItemUseCaseImpl(cachedAppConfigUseCase, greenCardUtil)

        assertFalse(coronaMelderUseCase.shouldShowCoronaMelderItem(emptyList(), DatabaseSyncerResult.Failed.Error(
            AppErrorResult(
                HolderStep.TestResultNetworkRequest,
                IllegalStateException()
            )
        )))
    }
}