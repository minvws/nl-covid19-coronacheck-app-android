package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject

interface ReadEuropeanCredentialUtil {
    fun getDosesForVaccination(readEuropeanCredential: JSONObject): String
    fun getHighestAndTotalDose(readEuropeanCredential: JSONObject): Pair<String, String>
}

class ReadEuropeanCredentialUtilImpl(private val application: Application) :
    ReadEuropeanCredentialUtil {

    override fun getDosesForVaccination(readEuropeanCredential: JSONObject): String {
        val (highestDose, totalDoses) = getHighestAndTotalDose(readEuropeanCredential)

        val doses =
            if (highestDose.isNotEmpty() && totalDoses.isNotEmpty()) {
                application.getString(
                    R.string.your_vaccination_explanation_doses_answer,
                    highestDose,
                    totalDoses
                )
            } else ""

        return doses
    }

    override fun getHighestAndTotalDose(readEuropeanCredential: JSONObject): Pair<String, String> {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc?.getJSONArray("v")?.optJSONObject(0)
        val highestDose = vaccination?.getStringOrNull("dn")
        val totalDoses = vaccination?.getStringOrNull("sd")
        return Pair(highestDose ?: "", totalDoses ?: "")
    }
}