/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes.utils

import android.content.res.Resources
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination

interface LastVaccinationDoseUtil {

    fun getIsLastDoseAnswer(event: RemoteEventVaccination): String
}

class LastVaccinationDoseUtilImpl(
    private val resources: Resources
) : LastVaccinationDoseUtil {

    override fun getIsLastDoseAnswer(event: RemoteEventVaccination) =
        event.vaccination?.run {
            when {
                completed() && completionReason == "first-vaccination-elsewhere" -> resources.getString(R.string.holder_eventdetails_vaccinationStatus_firstVaccinationElsewhere)
                completed() && completionReason == "recovery" -> resources.getString(R.string.holder_eventdetails_vaccinationStatus_recovery)
                completed() && completionReason.isNullOrEmpty() -> resources.getString(R.string.holder_eventdetails_vaccinationStatus_complete)
                else -> ""
            }
        } ?: ""

    private fun RemoteEventVaccination.Vaccination.completed() =
        completedByMedicalStatement == true || completedByPersonalStatement == true

    private fun RemoteEventVaccination.Vaccination.notCompleted() =
        completedByMedicalStatement == false || completedByPersonalStatement == false
}
