package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface ReadEuropeanCredentialUtil {
    fun getDosesForVaccination(readEuropeanCredential: JSONObject): String
    fun getHighestAndTotalDose(readEuropeanCredential: JSONObject): Pair<String, String>
    fun shouldBeHiddenVaccination(readEuropeanCredential: JSONObject): Boolean
}

class ReadEuropeanCredentialUtilImpl(
    private val application: Application,
    private val clock: Clock
) : ReadEuropeanCredentialUtil {

    private companion object {
        const val VACCINATION_HIDDEN_AFTER_DAYS = 25L
    }

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
        val vaccination = getVaccination(readEuropeanCredential)
        return getDosesFromVaccination(vaccination)
    }


    override fun shouldBeHiddenVaccination(readEuropeanCredential: JSONObject): Boolean {
        val vaccination = getVaccination(readEuropeanCredential)
        val (highestDose, totalDoses) = getDosesFromVaccination(vaccination)
        val date = LocalDate.parse(vaccination?.getStringOrNull("dt"))
            ?.atStartOfDay()
            ?.atOffset(ZoneOffset.UTC)
        return date?.let {
            it.plusDays(VACCINATION_HIDDEN_AFTER_DAYS) < OffsetDateTime.now(clock) && highestDose < totalDoses
        } ?: false
    }

    private fun getDosesFromVaccination(vaccination: JSONObject?): Pair<String, String> {
        val highestDose = vaccination?.getStringOrNull("dn")
        val totalDoses = vaccination?.getStringOrNull("sd")
        return Pair(highestDose ?: "", totalDoses ?: "")
    }

    private fun getVaccination(readEuropeanCredential: JSONObject): JSONObject? {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc?.getJSONArray("v")?.optJSONObject(0)
        return vaccination
    }
}