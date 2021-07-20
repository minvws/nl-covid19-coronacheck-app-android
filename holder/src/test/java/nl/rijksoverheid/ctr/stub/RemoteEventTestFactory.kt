package nl.rijksoverheid.ctr.stub

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import java.time.LocalDate

object RemoteEventTestFactory {

    fun createRemoteVaccination(
        type: String? = "type",
        unique: String? = "unique",
        date: LocalDate? = LocalDate.of(2021, 1, 1),
        hpkCode: String? = "hpkCode",
        vaccinationType: String? = "vaccinationType",
        brand: String? = "brand",
        completedByMedicalStatement: Boolean? = false,
        completedByPersonalStatement: Boolean? = false,
        completionReason: String? = "completionReason",
        doseNumber: String? = "doseNumber",
        totalDoses: String? = "totalDoses",
        country: String? = "country",
        manufacturer: String? = "manufacturer",
    ) = RemoteEventVaccination(
        type,
        unique,
        RemoteEventVaccination.Vaccination(
            date,
            hpkCode,
            vaccinationType,
            brand,
            completedByMedicalStatement,
            completedByPersonalStatement,
            completionReason,
            doseNumber,
            totalDoses,
            country,
            manufacturer
        )
    )
}