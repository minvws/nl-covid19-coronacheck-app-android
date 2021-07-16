package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.OffsetDateTime

interface ValidatePaperProofUseCase {
    suspend fun validate(qrContent: String, couplingCode: String): ValidatePaperProofResult
}

class ValidatePaperProofUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val mobileCoreWrapper: MobileCoreWrapper
) : ValidatePaperProofUseCase {

    override suspend fun validate(
        qrContent: String,
        couplingCode: String
    ): ValidatePaperProofResult {
        if (qrContent.startsWith("NL")) {
            return ValidatePaperProofResult.Error.DutchQr
        }

        try {
            val couplingResponse = coronaCheckRepository.getCoupling(
                credential = qrContent,
                couplingCode = couplingCode
            )

            return when (couplingResponse.status) {
                RemoteCouplingStatus.Accepted -> getEvent(qrContent)
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

    fun getEvent(qrCode: String): ValidatePaperProofResult {
        return try {
            val credential = qrCode.toByteArray()
            val credentials = mobileCoreWrapper.readEuropeanCredential(credential)
            val dcc = credentials.optJSONObject("dcc")
            val holder = getHolder(dcc!!)
            val event = getRemoteEvent(dcc)

            val protocol = RemoteProtocol3(
                providerIdentifier = "DCC",
                protocolVersion = "3.0",
                status = RemoteProtocol.Status.COMPLETE,
                holder = holder,
                events = listOf(event)
            )

            ValidatePaperProofResult.Success(mapOf(protocol to credential))
        } catch (exception: Exception) {
            ValidatePaperProofResult.Error.InvalidQr
        }
    }

    @Throws(NullPointerException::class)
    private fun getHolder(dcc: JSONObject): RemoteProtocol3.Holder {
        val fullName = dcc.optJSONObject("nam") ?: throw NullPointerException("can't parse name")
        return RemoteProtocol3.Holder(
            infix = "",
            firstName = fullName.getStringOrNull("gn"),
            lastName = fullName.getStringOrNull("fn"),
            birthDate = dcc.getStringOrNull("dob")
        )
    }

    @Throws(JSONException::class)
    private fun getRemoteEvent(dcc: JSONObject): RemoteEvent {
        return getRemoteVaccination(dcc) ?: getRemoteRecovery(dcc) ?: getRemoteTest(dcc)
        ?: throw JSONException("can't parse event type")
    }

    private fun getRemoteVaccination(dcc: JSONObject): RemoteEventVaccination? {
        return getEventByType(dcc, "v")?.let {
            RemoteEventVaccination(
                type = "vaccination",
                unique = it.getStringOrNull("ci"),
                vaccination = RemoteEventVaccination.Vaccination(
                    doseNumber = it.getStringOrNull("dn"),
                    totalDoses = it.getStringOrNull("sd"),
                    date = LocalDate.parse(it.getStringOrNull("dt")),
                    country = it.getStringOrNull("co"),
                    type = it.getStringOrNull("vp"),
                    brand = it.getStringOrNull("mp"),
                    manufacturer = it.getStringOrNull("ma"),
                    completedByMedicalStatement = null,
                    hpkCode = null
                )
            )
        }
    }

    private fun getRemoteRecovery(dcc: JSONObject): RemoteEventRecovery? {
        return getEventByType(dcc, "r")?.let {
            RemoteEventRecovery(
                type = "recovery",
                unique = it.getStringOrNull("ci") ?: "",
                isSpecimen = false,
                recovery = RemoteEventRecovery.Recovery(
                    sampleDate = LocalDate.parse(it.getStringOrNull("fr")),
                    validFrom = LocalDate.parse(it.getStringOrNull("df")),
                    validUntil = LocalDate.parse(it.getStringOrNull("du")),
                )
            )
        }
    }

    private fun getRemoteTest(dcc: JSONObject): RemoteEventNegativeTest? {
        return getEventByType(dcc, "t")?.let {
            RemoteEventNegativeTest(
                type = "test",
                unique = it.getStringOrNull("ci"),
                isSpecimen = false,
                negativeTest = RemoteEventNegativeTest.NegativeTest(
                    sampleDate = OffsetDateTime.parse(it.getStringOrNull("sc")),
                    negativeResult = it.getStringOrNull("tr") == "260415000",
                    facility = it.getStringOrNull("tc"),
                    type = it.getStringOrNull("tt"),
                    name = it.getStringOrNull("nm"),
                    manufacturer = it.getStringOrNull("ma")
                )
            )
        }
    }

    private fun getEventByType(dcc: JSONObject, key: String) = try {
        dcc.getJSONArray(key).optJSONObject(0)
    } catch (exception: JSONException) {
        null
    }
}

sealed class ValidatePaperProofResult {
    data class Success(val events: Map<RemoteProtocol3, ByteArray>) : ValidatePaperProofResult()

    sealed class Error : ValidatePaperProofResult() {
        object DutchQr : ValidatePaperProofResult()
        object InvalidQr : ValidatePaperProofResult()
        object ExpiredQr : ValidatePaperProofResult()
        object RejectedQr : ValidatePaperProofResult()
        object BlockedQr : ValidatePaperProofResult()
        data class ServerError(val httpCode: Int) : ValidatePaperProofResult()
        object NetworkError : ValidatePaperProofResult()
    }
}