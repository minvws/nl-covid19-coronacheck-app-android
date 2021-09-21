package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.shared.models.JSON
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase
) : RemoteEventHolderUtil {
    override fun holder(data: ByteArray, providerIdentifier: String): RemoteProtocol3.Holder? {
        val remoteEvent =
            if (providerIdentifier == RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC) {
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
            val storedBirthDay = birthDay(storedEventHolder.birthDate!!)
            val storedBirthMonth = birthMonth(storedEventHolder.birthDate)
            val storedFirstName = storedEventHolder.firstName!!
            val storedLastName = storedEventHolder.lastName!!
            incomingEventHolders.forEach { incomingEventHolder ->
                val incomingBirthDay = birthDay(incomingEventHolder.birthDate!!)
                val incomingBirthMonth = birthMonth(incomingEventHolder.birthDate)
                val birthDateIsNotMatching =
                    storedBirthDay != incomingBirthDay || storedBirthMonth != incomingBirthMonth
                val incomingFirstName = incomingEventHolder.firstName!!
                val incomingLastName = incomingEventHolder.lastName!!
                val nameIsNotMatching = nameIsNotMatching(storedFirstName, incomingFirstName) && nameIsNotMatching(storedLastName, incomingLastName)
                return birthDateIsNotMatching || nameIsNotMatching
            }
        }
        return false
    }

    private fun nameIsNotMatching(stored: String, incoming: String): Boolean {
        val storedNameInitial = stored.firstOrNull { it.isLetter() } ?: return false
        val incomingNameInitial = incoming.firstOrNull { it.isLetter() } ?: return false

        val input = "$storedNameInitial$incomingNameInitial"
        if (!latinCharactersRegex.matches(input)) return false

        return storedNameInitial != incomingNameInitial

    }

    private fun birthMonth(birthDate: String): Int {
        return LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).monthValue
    }

    private fun birthDay(birthDate: String): Int {
        return LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).dayOfMonth
    }

    companion object {
        private val latinCharactersRegex = Regex(pattern = "[A-Za-z]{2}")
    }
}

@JsonClass(generateAdapter = true)
data class SignedResponse(val signature: String, val payload: String) : JSON()
