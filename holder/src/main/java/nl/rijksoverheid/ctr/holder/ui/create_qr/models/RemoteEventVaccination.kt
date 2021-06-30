package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventVaccination(
    override val type: String?,
    val unique: String?,
    @Json(name = "vaccination") val vaccination: Vaccination?
) : Parcelable, RemoteEvent(type) {

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

    override fun getDate(): OffsetDateTime? {
        return vaccination?.date?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }

}