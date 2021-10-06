package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodeDataTest {

    @Test
    fun `European vaccination is over vaccinated when highest dose exceeds total dose`() {
        val vaccination1 = QrCodeData.European.Vaccination(
            highestDose = "1",
            ofTotalDoses = "2",
            bitmap = mockk(),
            readEuropeanCredential = mockk(),
            isHidden = false
        )
        val vaccination2 = QrCodeData.European.Vaccination(
            highestDose = "2",
            ofTotalDoses = "2",
            bitmap = mockk(),
            readEuropeanCredential = mockk(),
            isHidden = false
        )
        val vaccination3 = QrCodeData.European.Vaccination(
            highestDose = "3",
            ofTotalDoses = "2",
            bitmap = mockk(),
            readEuropeanCredential = mockk(),
            isHidden = false
        )

        assertFalse(vaccination1.isOverVaccinated)
        assertFalse(vaccination2.isOverVaccinated)
        assertTrue(vaccination3.isOverVaccinated)
    }
}