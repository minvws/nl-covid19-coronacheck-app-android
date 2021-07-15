package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetEventFromQrUseCase {

    fun get(qrCode: String)
}

class GetEventFromQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper
) : GetEventFromQrUseCase {

    override fun get(qrCode: String) {
        val credentials = mobileCoreWrapper.readEuropeanCredential(qrCode.toByteArray())
        val dcc = credentials.optJSONObject("dcc")

        val protocol = RemoteProtocol3(
            providerIdentifier = "DCC",
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = getHolder(dcc!!),
            events = listOf(getRemoteEvent(dcc))
        )
    }

    private fun getHolder(dcc: JSONObject): RemoteProtocol3.Holder {
        val fullName = dcc.optJSONObject("nam")
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
        getEventByType(dcc, "r")
        return null
    }

    private fun getRemoteTest(dcc: JSONObject): RemoteEventNegativeTest? {
        getEventByType(dcc, "t")
        return null
    }

    private fun getEventByType(dcc: JSONObject, key: String) = try {
        dcc.getJSONArray(key).optJSONObject(0)
    } catch (exception: JSONException) {
        null
    }
}