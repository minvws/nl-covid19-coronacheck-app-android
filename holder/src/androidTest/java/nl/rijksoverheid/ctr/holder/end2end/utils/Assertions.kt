/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.model.written
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.scrollToBottomOfOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertContains
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertNotDisplayed
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertNotExist
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.buttonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.clickBack
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.containsText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.labelValuePairExist
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText
import nl.rijksoverheid.ctr.holder.end2end.wait.ViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.wait.Wait

object Assertions {

    fun assertOverview() {
        waitForText("Mijn bewijzen", 15)
        assertDisplayed("Menu")
    }

    // MARK: Retrieval

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

    // MARK: Certificates on Overview

    fun assertQrButtonIsEnabled(eventType: Event.Type) {
        card(eventType).buttonIsEnabled(true)
    }

    fun assertQrButtonIsDisabled(eventType: Event.Type) {
        card(eventType).buttonIsEnabled(false)
    }

    fun assertInternationalEventOnOverview(event: Event, dose: String? = null) {
        scrollToBottomOfOverview()
        Wait.until(ViewIsShown(onView(card(event.type)), true))
        when (event) {
            is Vaccination -> {
                card(Event.Type.Vaccination).containsText("Dosis $dose")
                card(Event.Type.Vaccination).containsText("Vaccinatiedatum: " + event.eventDate.written())
            }
            is PositiveTest -> {
                card(Event.Type.PositiveTest).containsText("Geldig tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card(Event.Type.NegativeTest).containsText("Type test: " + event.testType.value)
                card(Event.Type.NegativeTest).containsText("Testdatum: " + event.eventDate.recently())
            }
        }
    }

    fun assertNotYetValidInternationalEventOnOverview(event: Event) {
        scrollToBottomOfOverview()
        Wait.until(ViewIsShown(onView(card(event.type)), true))
        when (event) {
            is PositiveTest -> {
                card(Event.Type.PositiveTest).containsText("geldig vanaf " + event.validFrom!!.writtenWithoutYear())
                card(Event.Type.PositiveTest).containsText(" tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card(Event.Type.NegativeTest).containsText("Type test: " + event.testType.value)
                card(Event.Type.NegativeTest).containsText("geldig vanaf " + event.validFrom!!.written())
                card(Event.Type.NegativeTest).containsText("Wordt automatisch geldig")
            }
        }
    }

    fun assertInternationalEventWillBecomeValid(eventType: Event.Type) {
        Wait.until(ViewIsShown(onView(card(eventType)), true))
        card(eventType).containsText("Wordt automatisch geldig")
    }

    fun assertInternationalEventWillExpireSoon(eventType: Event.Type, daysLeft: Int) {
        Wait.until(ViewIsShown(onView(card(eventType)), true))
        card(eventType).containsText("Verloopt over $daysLeft dagen")
    }

    fun assertInternationalEventIsExpired(eventType: Event.Type) {
        waitForText("Je internationale ${eventType.domesticName.lowercase()} is verlopen")
    }

    // MARK: QR Details

    fun assertInternationalQRDetails(person: Person, event: Event, dose: String? = null, deviceDate: LocalDate = LocalDate.now()) {
        if (event is Vaccination) waitForText("Dosis $dose")
        Wait.until(ViewIsShown(onView(withText("Details")), true))
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
                val dateDiff = ChronoUnit.DAYS.between(event.eventDate, deviceDate)
                labelValuePairExist("Dagen sinds vaccinatie / Days since vaccination:", "$dateDiff dagen")
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
        waitForText("QR-code is verborgen")
        assertDisplayed("Wat betekent dit?")
        assertDisplayed("Laat toch zien")
    }

    fun assertQRisNotHidden() {
        assertNotExist("QR-code is verborgen")
        assertNotDisplayed("Wat betekent dit?")
        assertNotDisplayed("Laat toch zien")
    }

    fun assertQRisExpired() {
        waitForText("QR-code is verlopen")
        assertDisplayed("Wat betekent dit?")
        assertDisplayed("Laat toch zien")
    }

    fun assertNoPreviousQR() {
        assertNotDisplayed(R.id.previousQrButton)
    }
}
