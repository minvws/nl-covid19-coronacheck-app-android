/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.paper_proof.usecases

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetPaperProofTypeUseCaseImpl
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPaperProofTypeUseCaseImplTest {

    @Test
    fun `get returns DCC Foreign if qr is a foreign dcc`() = runBlocking {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        val usecase = GetPaperProofTypeUseCaseImpl(
            getEventsFromPaperProofQrUseCase = mockk(relaxed = true),
            paperProofUtil = mockk(relaxed = true),
            mobileCoreWrapper = mobileCoreWrapper
        )

        every { mobileCoreWrapper.isDcc("".toByteArray()) } answers { true }
        every { mobileCoreWrapper.isForeignDcc("".toByteArray()) } answers { true }

        val paperProofType = usecase.get("")
        assertTrue(paperProofType is PaperProofType.DCC.Foreign)
    }

    @Test
    fun `get returns DCC Dutch if qr is not a foreign dcc`() = runBlocking {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        val usecase = GetPaperProofTypeUseCaseImpl(
            getEventsFromPaperProofQrUseCase = mockk(relaxed = true),
            paperProofUtil = mockk(relaxed = true),
            mobileCoreWrapper = mobileCoreWrapper
        )

        every { mobileCoreWrapper.isDcc("".toByteArray()) } answers { true }
        every { mobileCoreWrapper.isForeignDcc("".toByteArray()) } answers { false }

        val paperProofType = usecase.get("")
        assertTrue(paperProofType is PaperProofType.DCC.Dutch)
    }

    @Test
    fun `get returns CTB if qr is CTB`() = runBlocking {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        val usecase = GetPaperProofTypeUseCaseImpl(
            getEventsFromPaperProofQrUseCase = mockk(relaxed = true),
            paperProofUtil = mockk(relaxed = true),
            mobileCoreWrapper = mobileCoreWrapper
        )

        every { mobileCoreWrapper.isDcc("".toByteArray()) } answers { false }
        every { mobileCoreWrapper.hasDomesticPrefix("".toByteArray()) } answers { true }

        val paperProofType = usecase.get("")
        assertTrue(paperProofType is PaperProofType.CTB)
    }

    @Test
    fun `get returns unknown if qr not CTB and not DCC`() = runBlocking {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        val usecase = GetPaperProofTypeUseCaseImpl(
            getEventsFromPaperProofQrUseCase = mockk(relaxed = true),
            paperProofUtil = mockk(relaxed = true),
            mobileCoreWrapper = mobileCoreWrapper
        )

        every { mobileCoreWrapper.isDcc("".toByteArray()) } answers { false }
        every { mobileCoreWrapper.hasDomesticPrefix("".toByteArray()) } answers { false }

        val paperProofType = usecase.get("")
        assertTrue(paperProofType is PaperProofType.Unknown)
    }
}