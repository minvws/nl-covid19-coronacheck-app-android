package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.OffsetDateTime

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventsRecovery(
    override val type: String?,
    val unique: String,
    val isSpecimen: Boolean,
    val recovery: Recovery?
) : Parcelable, RemoteEvent(type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Recovery(
        val sampleDate: OffsetDateTime?,
        val validFrom: LocalDate?,
        val validUntil: LocalDate?
    ): Parcelable
}