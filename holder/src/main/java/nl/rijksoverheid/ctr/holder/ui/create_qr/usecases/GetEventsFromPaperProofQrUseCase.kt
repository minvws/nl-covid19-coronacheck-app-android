package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime

interface GetEventsFromPaperProofQrUseCase {

    fun get(qrCode: String): RemoteProtocol3
}

class GetEventsFromPaperProofQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper
) : GetEventsFromPaperProofQrUseCase {

    @Throws(NullPointerException::class, JSONException::class)
    override fun get(qrCode: String): RemoteProtocol3 {
        val credential = qrCode.toByteArray()
        val credentials = mobileCoreWrapper.readEuropeanCredential(credential)
        val dcc = credentials.optJSONObject("dcc")
        val holder = getHolder(dcc!!)
        val event = getRemoteEvent(dcc)

        return RemoteProtocol3(
            providerIdentifier = RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC,
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder,
            events = listOf(event)
        )
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
                    hpkCode = null,
                    completedByPersonalStatement = null,
                    completionReason = null
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
        return getEventByType(dcc, "t")?.let { jsonObject ->
            RemoteEventNegativeTest(
                type = "test",
                unique = jsonObject.getStringOrNull("ci"),
                isSpecimen = false,
                negativeTest = RemoteEventNegativeTest.NegativeTest(
                    sampleDate = OffsetDateTime.parse(jsonObject.getStringOrNull("sc")),
                    negativeResult = jsonObject.getStringOrNull("tr") == "260415000",
                    facility = jsonObject.getStringOrNull("tc"),
                    type = jsonObject.getStringOrNull("tt"),
                    name = jsonObject.getStringOrNull("nm")
                        .takeIf { it?.isNotEmpty() ?: false },
                    manufacturer = jsonObject.getStringOrNull("ma")
                        .takeIf { it?.isNotEmpty() ?: false }
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