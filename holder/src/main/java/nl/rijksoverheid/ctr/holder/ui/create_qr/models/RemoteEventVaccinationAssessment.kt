package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventVaccinationAssessment(
    override val type: String?,
    override val unique: String?,
    @Json(name = "vaccinationassessment") val vaccinationAssessment: VaccinationAssessment
) : Parcelable, RemoteEvent(unique, type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class VaccinationAssessment(
        val assessmentDate: OffsetDateTime?,
        val digitallyVerified: Boolean?,
        val country: String?,
    ) : Parcelable

    override fun getDate(): OffsetDateTime? {
        return vaccinationAssessment.assessmentDate
    }
}
