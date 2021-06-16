package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventsVaccinations(
    val events: List<Event>?,
    override val protocolVersion: String,
    override val providerIdentifier: String,
    override val status: Status,
    val holder: Holder?
) : RemoteProtocol(providerIdentifier, protocolVersion, status), Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Holder(
        val infix: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Event(
        val type: String?,
        val unique: String?,
        val vaccination: Vaccination?
    ) : Parcelable {

        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Vaccination(
            val date: LocalDate?,
            val hpkCode: String?,
            val type: String?,
            val brand: String?,
            val completedByMedicalStatement: Boolean?,
            val doseNumber: String?,
            val totalDoses: String?,
            val country: String?,
            val manufacturer: String?
        ) : Parcelable

    }

    override fun hasEvents(): Boolean {
        return events?.isNotEmpty() ?: false
    }
}

