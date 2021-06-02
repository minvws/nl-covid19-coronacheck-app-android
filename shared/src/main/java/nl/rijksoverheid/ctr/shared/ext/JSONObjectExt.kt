package nl.rijksoverheid.ctr.shared.ext

import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getStringOrNull(name: String): String? {
    return try {
        val string = getString(name)
        if (string.isNullOrEmpty()) {
            null
        } else {
            if (string == "null") {
                null
            } else {
                string
            }
        }
    } catch (e: JSONException) {
        null
    }
}