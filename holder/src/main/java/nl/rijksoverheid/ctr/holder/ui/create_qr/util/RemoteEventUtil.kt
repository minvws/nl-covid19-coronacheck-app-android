package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONException
import org.json.JSONObject
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime

interface RemoteEventUtil {
    fun getHolderFromDcc(dcc: JSONObject): RemoteProtocol3.Holder
    fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent>
    fun getRemoteEventFromDcc(dcc: JSONObject): RemoteEvent
    fun getRemoteVaccinationFromDcc(dcc: JSONObject): RemoteEventVaccination?
    fun getRemoteRecoveryFromDcc(dcc: JSONObject): RemoteEventRecovery?
    fun getRemoteTestFromDcc(dcc: JSONObject): RemoteEventNegativeTest?
    fun getRemoteEventsFromNonDcc(eventGroupEntity: EventGroupEntity): List<RemoteEvent>
    fun isRecoveryEventExpired(remoteEventRecovery: RemoteEventRecovery): Boolean
    fun isPositiveTestEventExpired(remoteEventPositiveTest: RemoteEventPositiveTest): Boolean
}

class RemoteEventUtilImpl(
    private val clock: Clock,
    private val moshi: Moshi,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase): RemoteEventUtil {

    /**
     * Only remove duplicate events for vaccination events
     */
    override fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent> {
        return if (remoteEvents.all { it is RemoteEventVaccination }) {
            return remoteEvents.filterIsInstance<RemoteEventVaccination>().distinct()
        } else {
            remoteEvents
        }
    }

    @Throws(NullPointerException::class)
    override fun getHolderFromDcc(dcc: JSONObject): RemoteProtocol3.Holder {
        val fullName = dcc.optJSONObject("nam") ?: throw NullPointerException("can't parse name")
        return RemoteProtocol3.Holder(
            infix = "",
            firstName = fullName.getStringOrNull("gn"),
            lastName = fullName.getStringOrNull("fn"),
            birthDate = dcc.getStringOrNull("dob")
        )
    }

    @Throws(JSONException::class)
    override fun getRemoteEventFromDcc(dcc: JSONObject): RemoteEvent {
        return getRemoteVaccinationFromDcc(dcc) ?: getRemoteRecoveryFromDcc(dcc) ?: getRemoteTestFromDcc(dcc)
        ?: throw JSONException("can't parse event type")
    }

    override fun getRemoteVaccinationFromDcc(dcc: JSONObject): RemoteEventVaccination? {
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

    override fun getRemoteRecoveryFromDcc(dcc: JSONObject): RemoteEventRecovery? {
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

    override fun getRemoteTestFromDcc(dcc: JSONObject): RemoteEventNegativeTest? {
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

    override fun getRemoteEventsFromNonDcc(eventGroupEntity: EventGroupEntity): List<RemoteEvent> {
        val payload = moshi.adapter(SignedResponse::class.java)
            .fromJson(String(eventGroupEntity.jsonData))?.payload
        val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
        return moshi.adapter(RemoteProtocol3::class.java).fromJson(decodedPayload)?.events ?: listOf()
    }

    override fun isRecoveryEventExpired(remoteEventRecovery: RemoteEventRecovery): Boolean {
        return OffsetDateTime.now(clock).minusDays(cachedAppConfigUseCase.getCachedAppConfig().recoveryEventValidityDays.toLong()) >= remoteEventRecovery.getDate()
    }

    override fun isPositiveTestEventExpired(remoteEventPositiveTest: RemoteEventPositiveTest): Boolean {
        return OffsetDateTime.now(clock).minusDays(cachedAppConfigUseCase.getCachedAppConfig().recoveryEventValidityDays.toLong()) >= remoteEventPositiveTest.getDate()
    }

    private fun getEventByType(dcc: JSONObject, key: String) = try {
        dcc.getJSONArray(key).optJSONObject(0)
    } catch (exception: JSONException) {
        null
    }
}