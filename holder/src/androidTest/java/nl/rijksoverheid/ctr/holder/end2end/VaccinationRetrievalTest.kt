package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination.VaccineType.Janssen
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination.VaccineType.Moderna
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination.VaccineType.Pfizer
import nl.rijksoverheid.ctr.holder.end2end.model.offset
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.backToOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalVaccinationOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalDetails
import org.junit.Test

class VaccinationRetrievalTest : BaseTest() {

    @Test
    fun retrieveVaccination_2Pfizer() {
        val person = Person(bsn = "999990020")
        val vac1 = Vaccination(eventDate = today.offset(-60), vaccine = Pfizer)
        val vac2 = Vaccination(eventDate = today.offset(-30), vaccine = Pfizer)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalVaccinationOnOverview(vac2, dose = "2/2")
        assertInternationalVaccinationOnOverview(vac1, dose = "1/2")

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR(hidden = true)
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        backToOverview()
    }

    @Test
    fun retrieveVaccination_2Moderna() {
        val person = Person("999990159")
        val vac1 = Vaccination(eventDate = today.offset(-60), vaccine = Moderna)
        val vac2 = Vaccination(eventDate = today.offset(-30), vaccine = Moderna)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalVaccinationOnOverview(vac2, dose = "2/2")
        assertInternationalVaccinationOnOverview(vac1, dose = "1/2")

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR(hidden = true)
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        backToOverview()
    }

    @Test
    fun retrieveVaccination_2Janssen() {
        val person = Person("999990093")
        val vac1 = Vaccination(eventDate = today.offset(-60), vaccine = Janssen)
        val vac2 = Vaccination(eventDate = today.offset(-30), vaccine = Janssen)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalVaccinationOnOverview(vac2, dose = "2/1")
        assertInternationalVaccinationOnOverview(vac1, dose = "1/1")

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/1")
        viewPreviousQR()
        assertInternationalQRDetails(person, vac1, dose = "1/1")
        backToOverview()
    }
}
