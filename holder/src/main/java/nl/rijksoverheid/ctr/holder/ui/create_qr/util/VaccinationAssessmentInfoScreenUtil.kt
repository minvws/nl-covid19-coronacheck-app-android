/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.Context
import android.text.TextUtils
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccinationAssessment

interface VaccinationAssessmentInfoScreenUtil {
    fun getForVaccinationAssessment(
        event: RemoteEventVaccinationAssessment,
        fullName: String,
        birthDate: String,
    ): InfoScreen
}

class VaccinationAssessmentInfoScreenUtilImpl(private val context: Context): VaccinationAssessmentInfoScreenUtil {
    override fun getForVaccinationAssessment(
        event: RemoteEventVaccinationAssessment,
        fullName: String,
        birthDate: String
    ): InfoScreen {
        return InfoScreen(
            title = context.resources.getString(R.string.your_vaccination_explanation_toolbar_title),
            description = TextUtils.concat(
                context.getString(R.string.holder_event_vaccination_assessment_about_subtitle),
                "<br/><br/>",
                createLine(
                    name = context.resources.getString(R.string.holder_event_vaccination_assessment_about_name),
                    nameAnswer = fullName
                ),
                createLine(
                    name = context.resources.getString(R.string.holder_event_vaccination_assessment_about_date_of_birth),
                    nameAnswer = birthDate,
                ),
                "<br/>",
                createLine(
                    name = context.resources.getString(R.string.holder_event_vaccination_assessment_about_date),
                    nameAnswer = event.vaccinationAssessment.assessmentDate?.formatDateTime(context) ?: "",
                ),
                "<br/>",
                createLine(
                    name = context.resources.getString(R.string.holder_event_vaccination_assessment_about_unique_identifier),
                    nameAnswer = event.unique ?: ""
                )
            ) as String
        )
    }

    private fun createLine(
        name: String,
        nameAnswer: String,
        isOptional: Boolean = false
    ): String {
        return if (isOptional && nameAnswer.isEmpty()) "" else "$name <b>$nameAnswer</b><br/>"
    }

}