/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.widgets

import android.content.Context
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination

interface YourEventWidgetUtil {
    fun getVaccinationEventTitle(
        context: Context,
        isDccEvent: Boolean,
        currentEvent: RemoteEventVaccination
    ): String

    fun getVaccinationEventSubtitle(
        context: Context,
        isDccEvent: Boolean,
        providerIdentifiers: String,
        vaccinationDate: String,
        fullName: String,
        birthDate: String
    ): String
}

class YourEventWidgetUtilImpl : YourEventWidgetUtil {
    override fun getVaccinationEventTitle(
        context: Context,
        isDccEvent: Boolean,
        currentEvent: RemoteEventVaccination
    ): String {
        return if (isDccEvent) {
            context.getString(R.string.retrieved_vaccination_dcc_title, currentEvent.vaccination?.doseNumber ?: "", currentEvent.vaccination?.totalDoses ?: "")
        } else {
            context.getString(
                R.string.retrieved_vaccination_title
            )
        }
    }

    override fun getVaccinationEventSubtitle(
        context: Context,
        isDccEvent: Boolean,
        providerIdentifiers: String,
        vaccinationDate: String,
        fullName: String,
        birthDate: String
    ): String {
        return if (isDccEvent) {
            context.getString(
                R.string.your_vaccination_dcc_row_subtitle,
                vaccinationDate,
                fullName,
                birthDate)
        } else {
            context.getString(
                R.string.your_vaccination_row_subtitle,
                vaccinationDate,
                fullName,
                birthDate,
                providerIdentifiers
            )
        }
    }
}
