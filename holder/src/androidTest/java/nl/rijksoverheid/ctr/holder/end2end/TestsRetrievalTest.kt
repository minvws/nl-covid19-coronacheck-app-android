package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalNegativeTestOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalRecoveryOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalDetails
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class TestsRetrievalTest : BaseTest() {

    @Test
    fun retrievePositiveTest() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(eventDate = today.offsetDays(-30), testType = TestEvent.TestType.Pcr, validUntil = today.offsetDays(150))

        addRecoveryCertificate(person.bsn)
        assertRetrievalDetails(person, pos)
        addRetrievedCertificateToApp()

        assertInternationalRecoveryOnOverview(pos)
        viewQR(Event.Type.PositiveTest)
        assertInternationalQRDetails(person, pos)
    }

    @Test
    fun retrieveNegativeTest() {
        val person = Person(bsn = "999992004")
        val neg = NegativeTest(eventDate = today, testType = TestEvent.TestType.Pcr)

        addNegativeTestCertificateFromGGD(person.bsn)
        assertRetrievalDetails(person, neg)
        addRetrievedCertificateToApp()

        assertInternationalNegativeTestOnOverview(neg)

        viewQR(Event.Type.NegativeTest)
        assertInternationalQRDetails(person, neg)
    }
}
