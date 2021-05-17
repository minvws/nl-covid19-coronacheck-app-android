package nl.rijksoverheid.ctr.api.json

import com.squareup.moshi.*
import java.time.LocalDate

class LocalDateJsonAdapter : JsonAdapter<LocalDate>() {

    @FromJson
    override fun fromJson(reader: JsonReader): LocalDate? {
        val date = reader.nextString()
        return LocalDate.parse(date)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: LocalDate?) {
        writer.value(value?.toString())
    }
}
