package nl.rijksoverheid.ctr.shared.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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
