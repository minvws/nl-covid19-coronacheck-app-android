package nl.rijksoverheid.ctr.shared.json

import com.squareup.moshi.*
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

class OffsetDateTimeJsonAdapter : JsonAdapter<OffsetDateTime>() {

    @FromJson
    override fun fromJson(reader: JsonReader): OffsetDateTime? {
        val date = reader.nextString()
        return OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: OffsetDateTime?) {
        val date: String? = value?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        writer.value(date)
    }
}
