package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCouplingStatus
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import org.json.JSONObject

interface ValidatePaperProofUseCase {
    suspend fun validate(qrContent: String, couplingCode: String): ValidatePaperProofResult
}

class ValidatePaperProofUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val getEventsFromPaperProofQr: GetEventsFromPaperProofQrUseCase
) : ValidatePaperProofUseCase {

    override suspend fun validate(
        qrContent: String,
        couplingCode: String
    ): ValidatePaperProofResult {
        if (qrContent.startsWith("NL")) {
            return ValidatePaperProofResult.Invalid.DutchQr
        }

        return try {
            val networkRequestResult = coronaCheckRepository.getCoupling(
                credential = qrContent,
                couplingCode = couplingCode
            )

            when (networkRequestResult) {
                is NetworkRequestResult.Success -> {
                    when (networkRequestResult.response.status) {
                        RemoteCouplingStatus.Accepted -> validateSuccess(qrContent, couplingCode)
                        RemoteCouplingStatus.Rejected -> ValidatePaperProofResult.Invalid.RejectedQr
                        RemoteCouplingStatus.Blocked -> ValidatePaperProofResult.Invalid.BlockedQr
                        RemoteCouplingStatus.Expired -> ValidatePaperProofResult.Invalid.ExpiredQr
                    }
                }
                is NetworkRequestResult.Failed -> {
                    ValidatePaperProofResult.Invalid.Error(networkRequestResult)
                }
            }
        } catch (e: Exception) {
            ValidatePaperProofResult.Invalid.Error(
                AppErrorResult(
                step = HolderStep.CouplingNetworkRequest,
                e = e)
            )
        }
    }

    private fun validateSuccess(
        qrContent: String,
        couplingCode: String
    ) = ValidatePaperProofResult.Valid(
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

sealed class ValidatePaperProofResult {
    data class Valid(val events: Map<RemoteProtocol3, ByteArray>) : ValidatePaperProofResult()
    sealed class Invalid : ValidatePaperProofResult() {
        object DutchQr : Invalid()
        object InvalidQr: Invalid()
        object ExpiredQr : Invalid()
        object RejectedQr : Invalid()
        object BlockedQr : Invalid()
        data class Error(val errorResult: ErrorResult) : Invalid()
    }
}