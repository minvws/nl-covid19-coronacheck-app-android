package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.res.Resources
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination

interface LastVaccinationDoseUtil {

    fun getIsLastDoseAnswer(event: RemoteEventVaccination): String
}

class LastVaccinationDoseUtilImpl(
    private val resources: Resources
) : LastVaccinationDoseUtil {

    override fun getIsLastDoseAnswer(event: RemoteEventVaccination) =
        event.vaccination?.run {
            when {
                completed() && completionReason == "priorevent" -> resources.getString(R.string.your_vaccination_explanation_last_dose_yes_prior_event)
                completed() && completionReason == "recovery" -> resources.getString(R.string.your_vaccination_explanation_last_dose_yes_recovery)
                completed() && completionReason.isNullOrEmpty() -> resources.getString(R.string.your_vaccination_explanation_last_dose_yes)
                else -> ""
            }
        } ?: ""


    private fun RemoteEventVaccination.Vaccination.completed() =
        completedByMedicalStatement == true || completedByPersonalStatement == true

    private fun RemoteEventVaccination.Vaccination.notCompleted() =
        completedByMedicalStatement == false || completedByPersonalStatement == false
}