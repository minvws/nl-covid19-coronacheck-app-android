/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.shared.models.JSON
import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface RemoteEventHolderUtil {
    fun holder(data: ByteArray, providerIdentifier: String): RemoteProtocol.Holder?
    fun conflicting(
        storedEventHolders: List<RemoteProtocol.Holder>,
        incomingEventHolders: List<RemoteProtocol.Holder>
    ): Boolean
}

class RemoteEventHolderUtilImpl(
    private val moshi: Moshi,
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase,
    private val remoteEventUtil: RemoteEventUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil
) : RemoteEventHolderUtil {
    override fun holder(data: ByteArray, providerIdentifier: String): RemoteProtocol.Holder? {
        val remoteEvent =
            if (remoteEventUtil.isDccEvent(providerIdentifier)) {
                val qr = JSONObject(String(data)).optString("credential")
                getEventsFromPaperProofQrUseCase.get(qr)
            } else {
                val payload =
                    moshi.adapter(SignedResponse::class.java).fromJson(String(data))!!.payload
                val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
                moshi.adapter(RemoteProtocol::class.java).fromJson(decodedPayload)!!
            }
        return remoteEvent.holder
    }

    /**
     * Compare the holder of the currently stored events with the holder of the new importing events
     * If the birth date or month are different, then the holders are conflicting and
     * we should keep only one of them.
     */
    override fun conflicting(
        storedEventHolders: List<RemoteProtocol.Holder>,
        incomingEventHolders: List<RemoteProtocol.Holder>
    ): Boolean {
        storedEventHolders.forEach { storedEventHolder ->
            val storedBirthdate = yourEventsFragmentUtil.getBirthDate(storedEventHolder)
            incomingEventHolders.forEach { incomingEventHolder ->
                val incomingBirthdate = yourEventsFragmentUtil.getBirthDate(incomingEventHolder)
                return birthDateIsNotMatching(storedBirthdate, incomingBirthdate)
            }
        }
        return false
    }

    private fun birthDateIsNotMatching(stored: String, incoming: String): Boolean {
        if (stored == incoming) {
            return false
        }

        val storedBirthdayParts = stored.split(" ")
        val incomingBirthdayParts = incoming.split(" ")

        val storedBirthDay: String? = storedBirthdayParts.getOrNull(0)
        val storedBirthMonth: String? = storedBirthdayParts.getOrNull(1)
        val incomingBirthDay: String? = incomingBirthdayParts.getOrNull(0)
        val incomingBirthMonth: String? = incomingBirthdayParts.getOrNull(1)

        return storedBirthDay != incomingBirthDay || storedBirthMonth != incomingBirthMonth
    }
}

@JsonClass(generateAdapter = true)
data class SignedResponse(val signature: String, val payload: String) : JSON()
