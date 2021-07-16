package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

class OriginTypeJsonAdapter {
    @FromJson
    fun fromJson(value: String?): OriginType = OriginType.fromTypeString(value ?: error("OriginType not known"))

    @ToJson
    fun toJson(value: OriginType): String = value.getTypeString()
}