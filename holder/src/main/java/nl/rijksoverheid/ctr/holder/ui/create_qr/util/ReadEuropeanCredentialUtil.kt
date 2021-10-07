package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject

interface ReadEuropeanCredentialUtil {
    fun getDose(readEuropeanCredential: JSONObject): String?
    fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String
}

class ReadEuropeanCredentialUtilImpl(private val application: Application): ReadEuropeanCredentialUtil {
    override fun getDose(readEuropeanCredential: JSONObject): String? {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc.getJSONArray("v").optJSONObject(0)
        return vaccination.getStringOrNull("dn")
    }

    override fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc.getJSONArray("v").optJSONObject(0)

        val doses =
            if (vaccination.getStringOrNull("dn") != null && vaccination.getStringOrNull("sd") != null) {
                application.getString(
                    R.string.your_vaccination_explanation_doses_answer,
                    vaccination.getStringOrNull("dn"),
                    vaccination.getStringOrNull("sd")
                )
            } else ""

        return doses
    }
}