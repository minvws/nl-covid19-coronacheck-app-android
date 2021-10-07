package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject

interface ReadEuropeanCredentialUtil {
    fun getDose(readEuropeanCredential: JSONObject): String?
    fun getOfTotalDoses(readEuropeanCredential: JSONObject): String?
    fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String
}

class ReadEuropeanCredentialUtilImpl(private val application: Application) :
    ReadEuropeanCredentialUtil {

    override fun getDose(readEuropeanCredential: JSONObject): String? {
        val vaccination = getVaccination(readEuropeanCredential)
        return vaccination?.getStringOrNull("dn")
    }

    override fun getOfTotalDoses(readEuropeanCredential: JSONObject): String? {
        val vaccination = getVaccination(readEuropeanCredential)
        return vaccination?.getStringOrNull("sd")
    }

    override fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String {
        val vaccination = getVaccination(readEuropeanCredential)
        val dose = vaccination?.getStringOrNull("dn") ?: ""
        val totalDoses = vaccination?.getStringOrNull("sd") ?: ""

        val doses =
            if (dose.isNotEmpty() && totalDoses.isNotEmpty()) {
                application.getString(
                    R.string.your_vaccination_explanation_doses_answer, dose, totalDoses
                )
            } else ""

        return doses
    }

    private fun getVaccination(readEuropeanCredential: JSONObject): JSONObject? {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc?.getJSONArray("v")?.optJSONObject(0)
        return vaccination
    }
}