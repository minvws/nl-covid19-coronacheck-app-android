/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.assertions

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapButton
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertContains
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertNotDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertNotExist
import nl.rijksoverheid.ctr.holder.end2end.interaction.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.labelValuePairExist
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.utils.dutch
import nl.rijksoverheid.ctr.holder.end2end.utils.recently

object QR {

    fun assertInternationalQRDetails(person: Person, event: Event, dose: String? = null, deviceDate: LocalDate = LocalDate.now()) {
        if (event is VaccinationEvent) waitUntilTextIsShown("Dosis $dose")
        waitUntilTextIsShown("Details")
        tapButton("Details")
        labelValuePairExist("Naam / Name:", person.name)
        labelValuePairExist("Geboortedatum / Date of birth*:", person.birthDate.dutch())
        when (event) {
            is VaccinationEvent -> {
                val split = dose!!.split("/").joinToString(" / ")
                assertContains("Over mijn dosis $split")
                labelValuePairExist("Ziekteverwekker / Disease targeted:", event.disease)
                labelValuePairExist("Vaccin / Vaccine:", event.vaccine.value)
                labelValuePairExist("Type vaccin / Vaccine type:", event.vaccine.type)
                labelValuePairExist("Vaccinproducent / Vaccine manufacturer:", event.vaccine.manufacturer)
                labelValuePairExist("Dosis / Number in series of doses:", split)
                labelValuePairExist("Vaccinatiedatum / Vaccination date*:", event.eventDate.dutch())
                val dateDiff = ChronoUnit.DAYS.between(event.eventDate, deviceDate)
                labelValuePairExist("Dagen sinds vaccinatie / Days since vaccination:", "$dateDiff dagen")
                labelValuePairExist("Gevaccineerd in / Vaccinated in:", event.country.internationalName)
            }
            is PositiveTest -> {
                labelValuePairExist("Ziekte waarvan hersteld / Disease recovered from:", event.disease)
                event.validFrom?.let { labelValuePairExist("Geldig vanaf / Valid from*:", it.dutch()) }
                event.validUntil?.let { labelValuePairExist("Geldig tot / Valid to*:", it.dutch()) }
                labelValuePairExist("Testdatum / Test date*:", event.eventDate.dutch())
                labelValuePairExist("Getest in / Tested in:", event.country.internationalName)
            }
            is NegativeTest -> {
                labelValuePairExist("Ziekteverwekker / Disease targeted:", event.disease)
                labelValuePairExist("Type test / Test type:", event.testType.value)
                labelValuePairExist("Testnaam / Test name:", event.testName)
                labelValuePairExist("Testdatum / Test date:", event.eventDate.recently())
                labelValuePairExist("Testuitslag / Test result:", "negatief (geen coronavirus vastgesteld) / negative (no coronavirus detected)")
                labelValuePairExist("Testlocatie / Test location:", event.testLocation.detailsName)
                labelValuePairExist("Getest in / Tested in:", event.country.internationalName)
            }
        }
        labelValuePairExist("Afgever certificaat / Certificate issuer:", event.issuer)
        clickBack()
    }

    fun assertQRisShown() {
        // TODO: Not yet implemented
    }

    fun assertQRisHidden() {
        waitUntilTextIsShown("QR-code is verborgen")
        assertDisplayed("Wat betekent dit?")
        assertDisplayed("Laat toch zien")
    }

    fun assertQRisNotHidden() {
        assertNotExist("QR-code is verborgen")
        assertNotDisplayed("Wat betekent dit?")
        assertNotDisplayed("Laat toch zien")
    }

    fun assertQRisExpired() {
        waitUntilTextIsShown("QR-code is verlopen")
        assertDisplayed("Wat betekent dit?")
        assertDisplayed("Laat toch zien")
    }

    fun assertNoPreviousQR() {
        assertNotDisplayed(R.id.previousQrButton)
    }
}
