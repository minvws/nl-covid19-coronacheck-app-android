package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventsPositiveTest(
    override val type: String?,
    val unique: String,
    val isSpecimen: Boolean,
    val positiveTest: PositiveTest?
) : Parcelable, RemoteEvent(type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class PositiveTest(
        val sampleDate: OffsetDateTime?,
        val positiveResult: Boolean?,
        val facility: String?,
        val type: String?,
        val name: String?,
        val manufacturer: String?
    ): Parcelable
}