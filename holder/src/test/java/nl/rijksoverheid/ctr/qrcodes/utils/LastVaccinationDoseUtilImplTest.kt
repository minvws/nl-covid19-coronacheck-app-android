package nl.rijksoverheid.ctr.qrcodes.utils

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.get_events.models.RemoteEventTestFactory
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.qrcodes.utils.LastVaccinationDoseUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class LastVaccinationDoseUtilImplTest {

    private val resources: Resources = mockk()

    private val util = LastVaccinationDoseUtilImpl(resources)

    @Test
    fun `when statement is completed and reason is prior event, answer is yes with corona`() {
        val answer = "ja, corona"
        every { resources.getString(R.string.holder_eventdetails_vaccinationStatus_firstVaccinationElsewhere) } returns answer

        val personalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = true,
            completionReason = "first-vaccination-elsewhere"
        )
        val medicalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByMedicalStatement = true,
            completionReason = "first-vaccination-elsewhere"
        )
        val allStatementVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = true,
            completedByMedicalStatement = true,
            completionReason = "first-vaccination-elsewhere"
        )

        assertEquals(util.getIsLastDoseAnswer(personalVaccination), answer)
        assertEquals(util.getIsLastDoseAnswer(medicalVaccination), answer)
        assertEquals(util.getIsLastDoseAnswer(allStatementVaccination), answer)
    }

    @Test
    fun `when statement is completed and reason is recovery, answer is yes with corona`() {
        val answer = "ja, corona ergens"
        every { resources.getString(R.string.holder_eventdetails_vaccinationStatus_recovery) } returns answer

        val personalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = true,
            completionReason = "recovery"
        )
        val medicalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByMedicalStatement = true,
            completionReason = "recovery"
        )
        val allStatementVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = true,
            completedByMedicalStatement = true,
            completionReason = "recovery"
        )

        assertEquals(util.getIsLastDoseAnswer(personalVaccination), answer)
        assertEquals(util.getIsLastDoseAnswer(medicalVaccination), answer)
        assertEquals(util.getIsLastDoseAnswer(allStatementVaccination), answer)
    }

    @Test
    fun `when statement is completed and no reason, answer is yes`() {
        val answer = "ja"
        every { resources.getString(R.string.holder_eventdetails_vaccinationStatus_complete) } returns answer

        val personalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = true,
            completionReason = null
        )
        val medicalVaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByMedicalStatement = true,
            completionReason = ""
        )

        assertEquals(util.getIsLastDoseAnswer(personalVaccination), answer)
        assertEquals(util.getIsLastDoseAnswer(medicalVaccination), answer)
    }

    @Test
    fun `when statement is not completed and, answer is empty`() {
        val vaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = false,
            completedByMedicalStatement = false
        )

        assertEquals(util.getIsLastDoseAnswer(vaccination), "")
    }

    @Test
    fun `when it's unknown, answer is empty`() {
        val vaccination = RemoteEventTestFactory.createRemoteVaccination(
            completedByPersonalStatement = null,
            completedByMedicalStatement = null
        )

        assertEquals(util.getIsLastDoseAnswer(vaccination), "")
    }

    @Test
    fun `when vaccination is null, answer is empty`() {
        val vaccination = RemoteEventTestFactory.createRemoteVaccination().copy(vaccination = null)

        assertEquals(util.getIsLastDoseAnswer(vaccination), "")
    }
}
