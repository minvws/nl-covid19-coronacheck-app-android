package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.fakeClockDevationUseCase
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.fakeGreenCardEntity
import nl.rijksoverheid.ctr.holder.fakeGreenCardUtil
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.GreenCardExpiredItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.CardsItem.CardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem.HeaderItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.DashboardItemUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.test.assertTrue

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

    @Test
    fun `multiple vaccination card items should be combined into 1`() {
        val util = DashboardItemUtilImpl(mockk(), mockk())

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