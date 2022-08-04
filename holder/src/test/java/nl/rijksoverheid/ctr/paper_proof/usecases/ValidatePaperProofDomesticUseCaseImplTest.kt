/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.paper_proof.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticResult
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingResponse
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingStatus
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticUseCaseImpl
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePaperProofDomesticUseCaseImplTest {

    @Test
    fun `validate returns valid when coupling code and qr content match`() = runBlocking {
        val usecase = createUseCase(
            remoteCouplingStatus = { RemoteCouplingStatus.Accepted }
        )

        val result = usecase.validate(
            qrContent = "",
            couplingCode = ""
        )

        assertTrue(result is PaperProofDomesticResult.Valid)
    }

    @Test
    fun `validate returns rejected when server returns rejected status`() = runBlocking {
        val usecase = createUseCase(
            remoteCouplingStatus = { RemoteCouplingStatus.Rejected }
        )

        val result = usecase.validate(
            qrContent = "",
            couplingCode = ""
        )

        assertTrue(result is PaperProofDomesticResult.Invalid.RejectedQr)
    }

    @Test
    fun `validate returns blocked when server returns blocked status`() = runBlocking {
        val usecase = createUseCase(
            remoteCouplingStatus = { RemoteCouplingStatus.Blocked }
        )

        val result = usecase.validate(
            qrContent = "",
            couplingCode = ""
        )

        assertTrue(result is PaperProofDomesticResult.Invalid.BlockedQr)
    }

    @Test
    fun `validate returns expired when server returns expired status`() = runBlocking {
        val usecase = createUseCase(
            remoteCouplingStatus = { RemoteCouplingStatus.Expired }
        )

        val result = usecase.validate(
            qrContent = "",
            couplingCode = ""
        )

        assertTrue(result is PaperProofDomesticResult.Invalid.ExpiredQr)
    }

    @Test
    fun `validate returns error when server returns error`() = runBlocking {
        val usecase = createUseCase(
            remoteCouplingStatus = {
                error("Someting went wrong")
            }
        )

        val result = usecase.validate(
            qrContent = "",
            couplingCode = ""
        )

        assertTrue(result is PaperProofDomesticResult.Invalid.Error)
    }

    private fun createUseCase(
        remoteCouplingStatus: (() -> RemoteCouplingStatus)
    ): ValidatePaperProofDomesticUseCaseImpl {
        val coronaCheckRepository: CoronaCheckRepository = mockk()
        val getEventsFromPaperProofQr: GetEventsFromPaperProofQrUseCase = mockk()
        val paperProofUtil: PaperProofUtil = mockk()

        // Mock server call so it returns accepted status
        coEvery { coronaCheckRepository.getCoupling(any(), any()) } answers {
            NetworkRequestResult.Success(
                RemoteCouplingResponse(
                    status = remoteCouplingStatus.invoke()
                )
            )
        }

        // Mock getting events from qr content
        coEvery { getEventsFromPaperProofQr.get(any()) } answers {
            RemoteProtocol(
                "", "", RemoteProtocol.Status.COMPLETE, null, listOf()
            )
        }

        // Mock our paper proof signature
        coEvery { paperProofUtil.getEventGroupJsonData(
            qrContent = any(),
            couplingCode = any())
        } answers {
            "".toByteArray()
        }

        return ValidatePaperProofDomesticUseCaseImpl(
            coronaCheckRepository = coronaCheckRepository,
            getEventsFromPaperProofQr = getEventsFromPaperProofQr,
            paperProofUtil = paperProofUtil
        )
    }
}
