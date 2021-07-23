package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.CredentialDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.OriginDao
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.DomesticCredentialAttributes
import org.json.JSONObject
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
    private val holderDatabase: HolderDatabase = mockk {
        every { greenCardDao() } returns greenCardDao
        every { credentialDao() } returns credentialDao
        every { originDao() } returns originDao
    }
    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)
    private val createDomesticGreenCardUseCase: CreateDomesticGreenCardUseCase = mockk(relaxed = true)
    private val createEuGreenCardUseCase: CreateEuGreenCardUseCase = mockk(relaxed = true)
    private val usecase: SyncRemoteGreenCardsUseCaseImpl = SyncRemoteGreenCardsUseCaseImpl(
        holderDatabase = holderDatabase,
        createDomesticGreenCardUseCase = createDomesticGreenCardUseCase,
        createEuGreenCardsUseCase = createEuGreenCardUseCase,
        mobileCoreWrapper = mobileCoreWrapper
    )

    @Test
    fun `execute only creates european green cards if there is only a remote european green card`() = runBlocking {
        val euGreenCard = RemoteGreenCards.EuGreenCard(
            origins = listOf(),
            credential = ""
        )

        usecase.execute(
            remoteGreenCards = RemoteGreenCards(
                domesticGreencard = null,
                euGreencards = listOf(euGreenCard)
            )
        )

        coVerify(exactly = 0) { createDomesticGreenCardUseCase.create(any(), any()) }
        coVerify(exactly = 1) { createEuGreenCardUseCase.create(euGreenCard) }
    }

    @Test
    fun `execute only creates domestic green card if there is only a remote domestic green card`() = runBlocking {
        val createCredentials = "".toByteArray()

        val domesticCredentials = listOf(DomesticCredential(
            credential = JSONObject(),
            attributes = DomesticCredentialAttributes(
            birthDay = "",
            birthMonth = "",
            credentialVersion = 1,
            firstNameInitial = "",
            isSpecimen = "",
            lastNameInitial = "",
            isPaperProof = "",
            validFrom = 1,
            validForHours = 1
        )))

        val domesticGreenCard = RemoteGreenCards.DomesticGreenCard(
            origins = listOf(),
            createCredentialMessages = createCredentials
        )

        coEvery { mobileCoreWrapper.createDomesticCredentials(createCredentials) } answers { domesticCredentials }

        usecase.execute(
            remoteGreenCards = RemoteGreenCards(
                domesticGreencard = domesticGreenCard,
                euGreencards = listOf()
            )
        )

        coVerify(exactly = 1) { createDomesticGreenCardUseCase.create(domesticGreenCard, domesticCredentials) }
        coVerify(exactly = 0) { createEuGreenCardUseCase.create(any()) }
    }

    @Test
    fun `execute cleans up database`() = runBlocking {
        usecase.execute(
            RemoteGreenCards(
                domesticGreencard = null,
                euGreencards = listOf()
            )
        )

        coVerify(exactly = 1) { greenCardDao.deleteAll() }
        coVerify(exactly = 1) { originDao.deleteAll() }
        coVerify(exactly = 1) { credentialDao.deleteAll() }
    }
}