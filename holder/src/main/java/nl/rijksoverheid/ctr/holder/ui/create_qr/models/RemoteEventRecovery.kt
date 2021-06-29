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
data class RemoteEventRecovery(
    override val type: String?,
    val unique: String,
    val isSpecimen: Boolean,
    @Json(name = "recovery") val recovery: Recovery?
) : Parcelable, RemoteEvent(type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Recovery(
        val sampleDate: LocalDate?,
        val validFrom: LocalDate?,
        val validUntil: LocalDate?
    ) : Parcelable

    override fun getDate(): OffsetDateTime? {
        return recovery?.sampleDate?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }
}