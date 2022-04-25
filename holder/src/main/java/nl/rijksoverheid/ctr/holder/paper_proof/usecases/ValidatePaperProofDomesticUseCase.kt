/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingStatus
import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticResult
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import org.json.JSONObject

interface ValidatePaperProofDomesticUseCase {
    suspend fun validate(qrContent: String, couplingCode: String): PaperProofDomesticResult
}

class ValidatePaperProofDomesticUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val getEventsFromPaperProofQr: GetEventsFromPaperProofQrUseCase
) : ValidatePaperProofDomesticUseCase {

    override suspend fun validate(
        qrContent: String,
        couplingCode: String
    ): PaperProofDomesticResult {
        return try {
            val networkRequestResult = coronaCheckRepository.getCoupling(
                credential = qrContent,
                couplingCode = couplingCode
            )

            when (networkRequestResult) {
                is NetworkRequestResult.Success -> {
                    when (networkRequestResult.response.status) {
                        RemoteCouplingStatus.Accepted -> validateSuccess(qrContent, couplingCode)
                        RemoteCouplingStatus.Rejected -> PaperProofDomesticResult.Invalid.RejectedQr
                        RemoteCouplingStatus.Blocked -> PaperProofDomesticResult.Invalid.BlockedQr
                        RemoteCouplingStatus.Expired -> PaperProofDomesticResult.Invalid.ExpiredQr
                    }
                }
                is NetworkRequestResult.Failed -> {
                    PaperProofDomesticResult.Invalid.Error(networkRequestResult)
                }
            }
        } catch (e: Exception) {
            PaperProofDomesticResult.Invalid.Error(
                AppErrorResult(
                    step = HolderStep.CouplingNetworkRequest,
                    e = e
                )
            )
        }
    }

    private fun validateSuccess(
        qrContent: String,
        couplingCode: String
    ) = PaperProofDomesticResult.Valid(
        mapOf(
            getEventsFromPaperProofQr.get(qrContent) to getSignerCredential(qrContent, couplingCode)
        )
    )

    private fun getSignerCredential(
        qrCode: String,
        couplingCode: String
    ): ByteArray = JSONObject(
        mapOf(
            "credential" to qrCode,
            "couplingCode" to couplingCode
        )
    ).toString().toByteArray()
}