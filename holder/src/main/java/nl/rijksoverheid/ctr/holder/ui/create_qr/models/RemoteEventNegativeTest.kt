package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteEventNegativeTest(
    override val type: String?,
    val unique: String?,
    val isSpecimen: Boolean?,
    @Json(name = "negativetest") val negativeTest: NegativeTest?
) : Parcelable, RemoteEvent(type) {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class NegativeTest(
        val sampleDate: OffsetDateTime?,
        val negativeResult: Boolean?,
        val facility: String?,
        val type: String?,
        val name: String?,
        val manufacturer: String?
    ): Parcelable

    override fun getDate(): OffsetDateTime? {
        return negativeTest?.sampleDate
    }
}