/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.assertions

import nl.rijksoverheid.ctr.holder.end2end.interaction.assertContains
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.labelValuePairExist
import nl.rijksoverheid.ctr.holder.end2end.interaction.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.tapButton
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.written
import nl.rijksoverheid.ctr.holder.end2end.utils.recently

object Retrieval {

    fun assertRetrievalDetails(person: Person, event: Event, position: Int = 0) {
        waitUntilTextIsShown("Kloppen de gegevens?", 30)
        scrollTo("Klopt er iets niet?")
        tapButton("Details", position)
        assertContains("Naam: " + person.name)
        assertContains("Geboortedatum: " + person.birthDate.written())
        when (event) {
            is VaccinationEvent -> {
                assertContains("Ziekteverwekker: " + event.disease)
                assertContains("Vaccin: " + event.vaccine.value)
                assertContains("Type vaccin: " + event.vaccine.type)
                assertContains("Vaccinproducent: " + event.vaccine.manufacturer)
                assertContains("Vaccinatiedatum: " + event.eventDate.written())
                assertContains("Gevaccineerd in: " + event.country.domesticName)
            }
            else -> {
                event as TestEvent
                assertContains("Type test: " + event.testType.value)
                assertContains("Testnaam: " + event.testName)
                assertContains("Testdatum: " + event.eventDate.recently())
                assertContains("Testproducent: " + event.testProducer)
                assertContains("Testlocatie: " + event.testLocation.realName)
                assertContains("Getest in: " + event.country.domesticName)
            }
        }
        when (event) {
            is PositiveTest -> {
                assertContains("Testuitslag: positief (coronavirus vastgesteld)")
            }
            is NegativeTest -> {
                assertContains("Testuitslag: negatief (geen coronavirus vastgesteld)")
            }
        }
        clickBack()
    }

    fun assertSomethingWentWrong() {
        assertDisplayed("Sorry, er gaat iets mis")
        assertContains("Het is niet gelukt de server te bereiken")
        assertDisplayed("Sluiten")
        assertDisplayed("Probeer opnieuw")
    }

    fun assertRetrievalError(error: String) {
        waitUntilTextIsShown("Sorry, er gaat iets mis")
        labelValuePairExist("Foutcode:", error)
    }
}
