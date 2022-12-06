package nl.rijksoverheid.ctr.holder.end2end.utils

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickBack
import java.time.temporal.ChronoUnit
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.today
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.containsText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.labelValuePairExist
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText

object Assertions {

    fun assertOverview() {
        waitForText("Mijn bewijzen", 15)
        assertDisplayed("Menu")
    }

    fun assertRetrievalDetails(person: Person, event: Event, position: Int = 0) {
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
                assertContains("Testuitslag: positief (corona)")
            }
            is NegativeTest -> {
                assertContains("Type test: " + event.testType.value)
                assertContains("Testdatum: " + event.eventDate.recently())
                assertContains("Testuitslag: negatief (geen corona)")
            }
        }
        clickBack()
    }

    fun assertInternationalVaccinationOnOverview(vaccination: Vaccination, dose: String) {
        card(Event.Type.Vaccination).containsText("Dosis $dose")
        card(Event.Type.Vaccination).containsText("Vaccinatiedatum: " + vaccination.eventDate.written())
    }

    fun assertInternationalRecoveryOnOverview(recovery: PositiveTest) {
        assertOverview()
        card(Event.Type.PositiveTest).containsText(recovery.type.internationalName)
        recovery.validUntil?.let { card(Event.Type.PositiveTest).containsText("Geldig tot " + it.written()) }
        card(Event.Type.PositiveTest).containsText("Bekijk QR")
    }

    fun assertInternationalNegativeTestOnOverview(negativeTest: NegativeTest) {
        assertOverview()
        card(Event.Type.NegativeTest).containsText(negativeTest.type.internationalName)
        card(Event.Type.NegativeTest).containsText("Type test: " + negativeTest.testType.value)
        card(Event.Type.NegativeTest).containsText("Testdatum: " + negativeTest.eventDate.recently())
        card(Event.Type.NegativeTest).containsText("Bekijk QR")
    }

    fun assertInternationalQRDetails(person: Person, event: Event, dose: String? = null) {
        if (event is Vaccination) waitForText("Dosis $dose")
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
                labelValuePairExist("Vaccinatiedatum / Date of vaccination*:", event.eventDate.dutch())
                val dateDiff = ChronoUnit.DAYS.between(event.eventDate, today)
                labelValuePairExist("Dagen sinds vaccinatie / Days since vaccination:", dateDiff.toString())
                labelValuePairExist("Gevaccineerd in / Member state of vaccination:", event.country.internationalName)
            }
            is PositiveTest -> {
                labelValuePairExist("Ziekte waarvan hersteld / Disease recovered from:", event.disease)
                labelValuePairExist("Testdatum / Test date:", event.eventDate.dutch())
                labelValuePairExist("Getest in / Member state of test:", event.country.internationalName)
                event.validFrom?.let { labelValuePairExist("Geldig vanaf / Valid from*:", it.dutch()) }
                event.validUntil?.let { labelValuePairExist("Geldig tot / Valid to*:", it.dutch()) }
            }
            is NegativeTest -> {
                labelValuePairExist("Ziekteverwekker / Disease targeted:", event.disease)
                labelValuePairExist("Type test / Type of test:", event.testType.value)
                labelValuePairExist("Testdatum / Test date:", event.eventDate.recently())
                labelValuePairExist("Testuitslag / Test result:", "negatief (geen corona) / negative (no coronavirus)")
                labelValuePairExist("Getest in / Member state of test:", event.country.internationalName)
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
