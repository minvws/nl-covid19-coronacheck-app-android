package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders.EventProvider.Companion.PROVIDER_IDENTIFIER_DCC
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CheckNewRecoveryValidityUseCaseImplTest {
    private val removeExpiredEventsUseCase = mockk<RemoveExpiredEventsUseCase>(relaxed = true)

    private val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true).apply {
        every { getCachedAppConfig().recoveryGreenCardRevisedValidityLaunchDate } returns "2021-11-08T23:00:00Z"
    }

    private val persistenceManager = mockk<PersistenceManager>(relaxed = true)

    private val holderDatabase = mockk<HolderDatabase>(relaxed = true)

    private val originUtil = mockk<OriginUtil>(relaxed = true)

    private val clock = Clock.fixed(Instant.parse("2021-11-26T00:00:00.00Z"), ZoneId.of("UTC"))

    private val checkNewRecoveryValidityUseCase = CheckNewRecoveryValidityUseCaseImpl(
        clock,
        removeExpiredEventsUseCase,
        cachedAppConfigUseCase,
        persistenceManager,
        holderDatabase,
        originUtil
    )

    @Test
    fun `recovery event which has not expired and is not paper can be extended`() = runBlocking {
        coEvery { persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity() } returns true
        coEvery { holderDatabase.eventGroupDao().getAll() } returns getGGDRecoveryEvent()
        val domesticCards = getDomesticCards()
        val firstDomesticCardOrigins = domesticCards.first().origins
        coEvery { holderDatabase.greenCardDao().getAll() } returns domesticCards
        coEvery { originUtil.getOriginState(firstDomesticCardOrigins) } returns listOf(
            OriginState.Valid(
                firstDomesticCardOrigins.first()
            )
        )

        checkNewRecoveryValidityUseCase.check()

        coVerify(exactly = 1) { persistenceManager.setShowExtendDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 0) { persistenceManager.setShowRecoverDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 1) {
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(
                false
            )
        }
    }

    @Test
    fun `recovery event which has expired and is not paper can be reissued`() = runBlocking {
        coEvery { persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity() } returns true
        coEvery { holderDatabase.eventGroupDao().getAll() } returns getGGDRecoveryEvent()
        val domesticCards = getDomesticCards()
        val firstDomesticCardOrigins = domesticCards.first().origins
        coEvery { holderDatabase.greenCardDao().getAll() } returns domesticCards
        coEvery { originUtil.getOriginState(firstDomesticCardOrigins) } returns listOf(
            OriginState.Expired(
                firstDomesticCardOrigins.first()
            )
        )

        checkNewRecoveryValidityUseCase.check()

        coVerify(exactly = 0) { persistenceManager.setShowExtendDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 1) { persistenceManager.setShowRecoverDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 1) {
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(
                false
            )
        }
    }

    @Test
    fun `vaccination event cannot be extended`() = runBlocking {
        coEvery { persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity() } returns true
        coEvery { holderDatabase.eventGroupDao().getAll() } returns getGGDVaccinationEvent()
        val domesticCards = getDomesticCards()
        val firstDomesticCardOrigins = domesticCards.first().origins
        coEvery { holderDatabase.greenCardDao().getAll() } returns domesticCards
        coEvery { originUtil.getOriginState(firstDomesticCardOrigins) } returns listOf(
            OriginState.Expired(
                firstDomesticCardOrigins.first()
            )
        )

        checkNewRecoveryValidityUseCase.check()

        coVerify(exactly = 0) { persistenceManager.setShowExtendDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 0) { persistenceManager.setShowRecoverDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 1) {
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(
                false
            )
        }
    }

    @Test
    fun `paper recovery events cannot be extended`() = runBlocking {
        coEvery { persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity() } returns true
        coEvery { holderDatabase.eventGroupDao().getAll() } returns getPaperRecoveryEvent()
        val domesticCards = getDomesticCards()
        val firstDomesticCardOrigins = domesticCards.first().origins
        coEvery { holderDatabase.greenCardDao().getAll() } returns domesticCards
        coEvery { originUtil.getOriginState(firstDomesticCardOrigins) } returns listOf(
            OriginState.Expired(
                firstDomesticCardOrigins.first()
            )
        )

        checkNewRecoveryValidityUseCase.check()

        coVerify(exactly = 0) { persistenceManager.setShowExtendDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 0) { persistenceManager.setShowRecoverDomesticRecoveryInfoCard(true) }
        coVerify(exactly = 1) {
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(
                false
            )
        }
    }

    private fun getPaperRecoveryEvent() = listOf(
        EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = PROVIDER_IDENTIFIER_DCC,
            type = OriginType.Recovery,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "".toByteArray(),
        )
    )

    private fun getGGDVaccinationEvent() = listOf(
        EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = "GGD",
            type = OriginType.Vaccination,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "".toByteArray(),
        )
    )

    private fun getGGDRecoveryEvent() = listOf(
        EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = "GGD",
            type = OriginType.Recovery,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "".toByteArray(),
        )
    )

    private fun getDomesticCards() = listOf(
        GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic,
            ),
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Recovery,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(),
                    validFrom = OffsetDateTime.now(),
                ),
            ),
            credentialEntities = listOf(),
        )
    )
}