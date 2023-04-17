package nl.rijksoverheid.ctr.persistence.database.usecases

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.dao.CredentialDao
import nl.rijksoverheid.ctr.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.persistence.database.dao.OriginDao
import nl.rijksoverheid.ctr.persistence.database.dao.OriginHintDao
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SyncRemoteGreenCardsUseCaseImplTest {

    private val greenCardDao: GreenCardDao = mockk(relaxed = true)
    private val originDao: OriginDao = mockk(relaxed = true)
    private val credentialDao: CredentialDao = mockk(relaxed = true)
    private val originHintDao: OriginHintDao = mockk(relaxed = true)
    private val holderDatabase: HolderDatabase = mockk {
        every { greenCardDao() } returns greenCardDao
        every { credentialDao() } returns credentialDao
        every { originDao() } returns originDao
        every { originHintDao() } returns originHintDao
    }
    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)
    private val createEuGreenCardUseCase: CreateEuGreenCardUseCase = mockk(relaxed = true)
    private val usecase: SyncRemoteGreenCardsUseCaseImpl = SyncRemoteGreenCardsUseCaseImpl(
        holderDatabase = holderDatabase,
        createEuGreenCardsUseCase = createEuGreenCardUseCase,
        mobileCoreWrapper = mobileCoreWrapper
    )

    @Test
    fun `execute only creates european green cards if there is only a remote european green card`() =
        runBlocking {
            val euGreenCard = RemoteGreenCards.EuGreenCard(
                origins = listOf(),
                credential = ""
            )

            usecase.execute(
                remoteGreenCards = RemoteGreenCards(
                    euGreencards = listOf(euGreenCard),
                    blobExpireDates = listOf()
                ),
                secretKey = ""
            )

            coVerify(exactly = 1) { createEuGreenCardUseCase.create(euGreenCard) }
        }

    @Test
    fun `execute cleans up database`() = runBlocking {
        usecase.execute(
            RemoteGreenCards(
                euGreencards = listOf(),
                blobExpireDates = listOf()
            ),
            secretKey = ""
        )

        coVerify(exactly = 1) { greenCardDao.deleteAll() }
        coVerify(exactly = 1) { originDao.deleteAll() }
        coVerify(exactly = 1) { credentialDao.deleteAll() }
        coVerify(exactly = 1) { originHintDao.deleteAll() }
    }
}
