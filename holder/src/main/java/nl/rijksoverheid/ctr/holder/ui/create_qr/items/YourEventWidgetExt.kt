package nl.rijksoverheid.ctr.holder.ui.create_qr.items

import nl.rijksoverheid.ctr.design.ext.formatMonth
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination

fun YourEventWidget.getVaccinationEventTitle(
    isDccEvent: Boolean,
    currentEvent: RemoteEventVaccination
) = if (isDccEvent) {
    resources.getString(R.string.retrieved_vaccination_dcc_title)
} else {
    resources.getString(
        R.string.retrieved_vaccination_title,
        currentEvent.vaccination?.date?.formatMonth(),
    )
}

fun YourEventWidget.getVaccinationEventSubtitle(
    isDccEvent: Boolean,
    providerIdentifiers: String,
    fullName: String,
    birthDate: String,
) = if (isDccEvent) {
    resources.getString(
        R.string.your_vaccination_dcc_row_subtitle,
        fullName,
        birthDate)
} else {
    resources.getString(
        R.string.your_vaccination_row_subtitle,
        fullName,
        birthDate,
        providerIdentifiers
    )
}