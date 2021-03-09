package nl.rijksoverheid.ctr.shared.models

import com.squareup.moshi.Moshi

abstract class JSON {
    fun toJson(moshi: Moshi): String {
        return moshi.adapter(javaClass).toJson(this)
    }

}
