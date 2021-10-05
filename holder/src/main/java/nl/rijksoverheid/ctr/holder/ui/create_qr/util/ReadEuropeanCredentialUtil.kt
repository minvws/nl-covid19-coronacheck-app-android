package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface ReadEuropeanCredentialUtil {
    fun getDosesForVaccination(readEuropeanCredential: JSONObject): String
    fun getHighestAndTotalDose(readEuropeanCredential: JSONObject): Pair<String, String>
    fun getDate(readEuropeanCredential: JSONObject): OffsetDateTime?
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

        val date = LocalDate.parse(vaccination?.getStringOrNull("dt"))
        val offsetDate = date?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }

    override fun getDate(readEuropeanCredential: JSONObject): OffsetDateTime? {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc?.getJSONArray("v")?.optJSONObject(0)
        val date = LocalDate.parse(vaccination?.getStringOrNull("dt"))
        return date?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
    }
}