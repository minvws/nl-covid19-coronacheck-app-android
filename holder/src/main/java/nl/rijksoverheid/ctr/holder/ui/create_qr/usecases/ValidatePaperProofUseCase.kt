package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCouplingStatus
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import retrofit2.HttpException
import java.io.IOException

interface ValidatePaperProofUseCase {
    suspend fun validate(qrContent: String, couplingCode: String): ValidatePaperProofResult
}

class ValidatePaperProofUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper): ValidatePaperProofUseCase {
    override suspend fun validate(qrContent: String, couplingCode: String): ValidatePaperProofResult {
        if (qrContent.startsWith("NL")) {
            return ValidatePaperProofResult.Error.DutchQr
        }

        // Check if we can read the credential
        try {
            mobileCoreWrapper.readEuropeanCredential(qrContent.toByteArray())
        } catch (e: Exception) {
            return ValidatePaperProofResult.Error.InvalidQr
        }

        try {
            val couplingResponse = coronaCheckRepository.getCoupling(
                credential = qrContent,
                couplingCode = couplingCode
            )

            return when (couplingResponse.status) {
                RemoteCouplingStatus.Accepted -> {
                    // TODO: Convert qr to events
                    ValidatePaperProofResult.Success(mapOf())
                }
                RemoteCouplingStatus.Rejected -> ValidatePaperProofResult.Error.RejectedQr
                RemoteCouplingStatus.Blocked -> ValidatePaperProofResult.Error.BlockedQr
                RemoteCouplingStatus.Expired -> ValidatePaperProofResult.Error.ExpiredQr
            }
        } catch (e: HttpException) {
            return ValidatePaperProofResult.Error.ServerError(e.code())
        } catch (e: IOException) {
            return ValidatePaperProofResult.Error.NetworkError
        } catch (e: Exception) {
            return ValidatePaperProofResult.Error.ServerError(200)
        }
    }
}

sealed class ValidatePaperProofResult {
    // TODO Add events to success
    data class Success(val events: Map<RemoteProtocol3, ByteArray>): ValidatePaperProofResult()

    sealed class Error: ValidatePaperProofResult() {
        object DutchQr: Error()
        object InvalidQr: Error()
        object ExpiredQr: Error()
        object RejectedQr: Error()
        object BlockedQr: Error()
        data class ServerError(val httpCode: Int) : Error()
        object NetworkError : Error()
    }
}