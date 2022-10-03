/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import android.util.Base64
import com.squareup.moshi.Moshi
import java.time.LocalDate
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

interface RemoteEventUtil {
    fun isDccEvent(providerIdentifier: String): Boolean
    fun getHolderFromDcc(dcc: JSONObject): RemoteProtocol.Holder
    fun removeDuplicateEvents(remoteEvents: List<RemoteEvent>): List<RemoteEvent>
    fun getRemoteEventFromDcc(dcc: JSONObject): RemoteEvent
    fun getRemoteVaccinationFromDcc(dcc: JSONObject): RemoteEventVaccination?
    fun getRemoteRecoveryFromDcc(dcc: JSONObject): RemoteEventRecovery?
    fun getRemoteTestFromDcc(dcc: JSONObject): RemoteEventNegativeTest?
    fun getRemoteProtocol3FromNonDcc(eventGroupEntity: EventGroupEntity): RemoteProtocol?
    fun getOriginType(remoteEvent: RemoteEvent): OriginType
}

class RemoteEventUtilImpl(
    private val moshi: Moshi
) : RemoteEventUtil {

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

    override fun isDccEvent(providerIdentifier: String): Boolean {
        return providerIdentifier.startsWith(RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC)
    }

    @Throws(NullPointerException::class)
    override fun getHolderFromDcc(dcc: JSONObject): RemoteProtocol.Holder {
        val fullName = dcc.optJSONObject("nam") ?: throw NullPointerException("can't parse name")
        return RemoteProtocol.Holder(
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
                    date = try { LocalDate.parse(it.getStringOrNull("dt")?.take(10)) } catch (e: Exception) { null },
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
                    sampleDate = try { LocalDate.parse(it.getStringOrNull("fr")?.take(10)) } catch (e: Exception) { null },
                    validFrom = try { LocalDate.parse(it.getStringOrNull("df")?.take(10)) } catch (e: Exception) { null },
                    validUntil = try { LocalDate.parse(it.getStringOrNull("du")?.take(10)) } catch (e: Exception) { null }
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

    override fun getRemoteProtocol3FromNonDcc(eventGroupEntity: EventGroupEntity): RemoteProtocol? {
        val payload = moshi.adapter(SignedResponse::class.java)
            .fromJson(String(eventGroupEntity.jsonData))?.payload
        val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
        return moshi.adapter(RemoteProtocol::class.java).fromJson(decodedPayload)
    }

    private fun getEventByType(dcc: JSONObject, key: String) = try {
        dcc.getJSONArray(key).optJSONObject(0)
    } catch (exception: JSONException) {
        null
    }

    override fun getOriginType(remoteEvent: RemoteEvent): OriginType {
        return when (remoteEvent) {
            is RemoteEventVaccination -> OriginType.Vaccination
            is RemoteEventRecovery -> OriginType.Recovery
            is RemoteEventPositiveTest -> OriginType.Recovery
            is RemoteEventNegativeTest -> OriginType.Test
            is RemoteEventVaccinationAssessment -> OriginType.VaccinationAssessment
            else -> error("remote event not supported as origin type")
        }
    }
}
