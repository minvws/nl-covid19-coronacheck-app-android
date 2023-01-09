package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.model.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisNotHidden
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalDetails
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class VaccinationRetrievalTest : BaseTest() {

    @Test
    fun retrieveVaccinationWith2Pfizer_assertOverviewAndQRDetails() {
        val person = Person(bsn = "999990020")
        val vac1 = Vaccination(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = Vaccination(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR()
        assertQRisHidden()
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        assertNoPreviousQR()
    }

    @Test
    fun retrieveVaccinationWith2Moderna_assertOverviewAndQRDetails() {
        val person = Person("999990159")
        val vac1 = Vaccination(eventDate = today.offsetDays(-60), vaccine = VaccineType.Moderna)
        val vac2 = Vaccination(eventDate = today.offsetDays(-30), vaccine = VaccineType.Moderna)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR()
        assertQRisHidden()
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        assertNoPreviousQR()
    }

    @Test
    fun retrieveVaccinationWith2Janssen_assertOverviewAndQRDetails() {
        val person = Person("999990093")
        val vac1 = Vaccination(eventDate = today.offsetDays(-60), vaccine = VaccineType.Janssen)
        val vac2 = Vaccination(eventDate = today.offsetDays(-30), vaccine = VaccineType.Janssen)

        addVaccinationCertificate(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/1")
        assertInternationalEventOnOverview(vac1, dose = "1/1")
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
        assertInternationalQRDetails(person, vac2, dose = "2/1")
        viewPreviousQR()
        assertQRisNotHidden()
        assertInternationalQRDetails(person, vac1, dose = "1/1")
        assertNoPreviousQR()
    }
}
