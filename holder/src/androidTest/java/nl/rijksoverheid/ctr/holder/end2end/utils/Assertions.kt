/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.utils

import java.time.temporal.ChronoUnit
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.today
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertContains
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.clickBack
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.containsText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.labelValuePairExist
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.rest
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollToTextInOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText

object Assertions {

    fun assertOverview() {
        waitForText("Mijn bewijzen")
        assertDisplayed("Menu")
    }

    fun assertRetrievalDetails(person: Person, event: Event, position: Int = 0) {
        waitForText("Kloppen de gegevens?", 30)
        scrollTo("Klopt er iets niet?")
        tapButton("Details", position)
        assertContains("Naam: " + person.name)
        assertContains("Geboortedatum: " + person.birthDate.written())
        when (event) {
            is Vaccination -> {
                assertContains("Ziekteverwekker: " + event.disease)
                assertContains("Vaccin: " + event.vaccine.value)
                assertContains("Vaccinatiedatum: " + event.eventDate.written())
                assertContains("Gevaccineerd in: " + event.country.domesticName)
            }
            is PositiveTest -> {
                assertContains("Type test: " + event.testType.value)
                assertContains("Testdatum: " + event.eventDate.recently())
                assertContains("Testuitslag: positief (coronavirus vastgesteld)")
            }
            is NegativeTest -> {
                assertContains("Type test: " + event.testType.value)
                assertContains("Testdatum: " + event.eventDate.recently())
                assertContains("Testuitslag: negatief (geen coronavirus vastgesteld)")
            }
        }
        clickBack()
    }

    fun assertRetrievalError(error: String) {
        waitForText("Sorry, er gaat iets mis")
        labelValuePairExist("Foutcode:", error)
    }

    fun assertInternationalVaccinationOnOverview(vaccination: Vaccination, dose: String) {
        assertOverview()
        scrollToTextInOverview("BEKIJK QR")
        card(Event.Type.Vaccination).containsText("Dosis $dose")
        card(Event.Type.Vaccination).containsText("Vaccinatiedatum: " + vaccination.eventDate.written())
    }

    fun assertInternationalRecoveryOnOverview(recovery: PositiveTest) {
        assertOverview()
        scrollToTextInOverview("BEKIJK QR")
        recovery.validUntil?.let { card(Event.Type.PositiveTest).containsText("Geldig tot " + it.written()) }
        card(Event.Type.PositiveTest).containsText("Bekijk QR")
    }

    fun assertInternationalNegativeTestOnOverview(negativeTest: NegativeTest) {
        assertOverview()
        scrollToTextInOverview("BEKIJK QR")
        card(Event.Type.NegativeTest).containsText("Type test: " + negativeTest.testType.value)
        card(Event.Type.NegativeTest).containsText("Testdatum: " + negativeTest.eventDate.recently())
        card(Event.Type.NegativeTest).containsText("Bekijk QR")
    }

    fun assertInternationalQRDetails(person: Person, event: Event, dose: String? = null) {
        if (event is Vaccination) waitForText("Dosis $dose")
        rest() // Waiting until 'Details' is clickable is unreliable
        tapButton("Details")
        labelValuePairExist("Naam / Name:", person.name)
        labelValuePairExist("Geboortedatum / Date of birth*:", person.birthDate.dutch())
        when (event) {
            is Vaccination -> {
                val split = dose!!.split("/").joinToString(" / ")
                assertContains("Over mijn dosis $split")
                labelValuePairExist("Ziekteverwekker / Disease targeted:", event.disease)
                labelValuePairExist("Vaccin / Vaccine:", event.vaccine.value)
                labelValuePairExist("Dosis / Number in series of doses:", split)
                labelValuePairExist("Vaccinatiedatum / Vaccination date*:", event.eventDate.dutch())
                val dateDiff = ChronoUnit.DAYS.between(event.eventDate, today)
                labelValuePairExist("Dagen sinds vaccinatie / Days since vaccination:", dateDiff.toString())
                labelValuePairExist("Gevaccineerd in / Vaccinated in:", event.country.internationalName)
            }
            is PositiveTest -> {
                labelValuePairExist("Ziekte waarvan hersteld / Disease recovered from:", event.disease)
                labelValuePairExist("Testdatum / Test date*:", event.eventDate.dutch())
                labelValuePairExist("Getest in / Tested in:", event.country.internationalName)
                event.validFrom?.let { labelValuePairExist("Geldig vanaf / Valid from*:", it.dutch()) }
                event.validUntil?.let { labelValuePairExist("Geldig tot / Valid to*:", it.dutch()) }
            }
            is NegativeTest -> {
                labelValuePairExist("Ziekteverwekker / Disease targeted:", event.disease)
                labelValuePairExist("Type test / Type of test:", event.testType.value)
                labelValuePairExist("Testdatum / Test date:", event.eventDate.recently())
                labelValuePairExist("Testuitslag / Test result:", "negatief (geen coronavirus vastgesteld) / negative (no coronavirus detected)")
                labelValuePairExist("Getest in / Tested in:", event.country.internationalName)
            }
        }
        clickBack()
    }

    fun assertQRisHidden() {
        assertDisplayed("QR-code is verborgen")
        assertDisplayed("Wat betekent dit?")
        assertDisplayed("Laat toch zien")

        tapButton("Wat betekent dit?")
        assertDisplayed("Verborgen QR-code")
        clickBack()

        tapButton("Laat toch zien")
    }
}
