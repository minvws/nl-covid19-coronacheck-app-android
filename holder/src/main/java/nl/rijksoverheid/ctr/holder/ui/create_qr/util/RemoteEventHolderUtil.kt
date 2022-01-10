package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsFromPaperProofQrUseCase
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
    fun holder(data: ByteArray, providerIdentifier: String): RemoteProtocol3.Holder?
    fun conflicting(
        storedEventHolders: List<RemoteProtocol3.Holder>,
        incomingEventHolders: List<RemoteProtocol3.Holder>
    ): Boolean
}

class RemoteEventHolderUtilImpl(
    private val moshi: Moshi,
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase,
    private val remoteEventUtil: RemoteEventUtil,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
) : RemoteEventHolderUtil {
    override fun holder(data: ByteArray, providerIdentifier: String): RemoteProtocol3.Holder? {
        val remoteEvent =
            if (remoteEventUtil.isDccEvent(providerIdentifier)) {
                val qr = JSONObject(String(data)).optString("credential")
                getEventsFromPaperProofQrUseCase.get(qr)
            } else {
                val payload =
                    moshi.adapter(SignedResponse::class.java).fromJson(String(data))!!.payload
                val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
                moshi.adapter(RemoteProtocol3::class.java).fromJson(decodedPayload)!!
            }
        return remoteEvent.holder
    }

    /**
     * Compare the holder of the currently stored events with the holder of the new importing events
     * If the birth date and one of the holder's names are different, then the holders are conflicting
     * we should keep only one of them.
     */
    override fun conflicting(
        storedEventHolders: List<RemoteProtocol3.Holder>,
        incomingEventHolders: List<RemoteProtocol3.Holder>
    ): Boolean {
        storedEventHolders.forEach { storedEventHolder ->
            // if any of the stored or the new data is null
            // then we cannot really compare, just go on and
            // let the user store his new card
            val storedBirthdate = yourEventsFragmentUtil.getBirthDate(storedEventHolder)
            val storedFirstName = storedEventHolder.firstName
            val storedLastName = storedEventHolder.lastName
            incomingEventHolders.forEach { incomingEventHolder ->
                val incomingBirthdate = yourEventsFragmentUtil.getBirthDate(incomingEventHolder)
                val birthDateIsNotMatching = birthDateIsNotMatching(storedBirthdate, incomingBirthdate)
                val incomingFirstName = incomingEventHolder.firstName
                val incomingLastName = incomingEventHolder.lastName
                val nameIsNotMatching = nameIsNotMatching(storedFirstName, incomingFirstName) && nameIsNotMatching(storedLastName, incomingLastName)
                return birthDateIsNotMatching || nameIsNotMatching
            }
        }
        return false
    }

    private fun nameIsNotMatching(stored: String?, incoming: String?): Boolean {
        if (stored == null || incoming == null) {
            return true
        }
        val storedNameInitial = stored.firstOrNull { it.isLetter() } ?: return false
        val incomingNameInitial = incoming.firstOrNull { it.isLetter() } ?: return false

        val input = "$storedNameInitial$incomingNameInitial"
        if (!latinCharactersRegex.matches(input)) return false

        return storedNameInitial != incomingNameInitial

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

    companion object {
        private val latinCharactersRegex = Regex(pattern = "[A-Za-z]{2}")
    }
}

@JsonClass(generateAdapter = true)
data class SignedResponse(val signature: String, val payload: String) : JSON()
