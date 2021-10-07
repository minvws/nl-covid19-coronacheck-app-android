package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData

interface MultipleQrCodesUtil {
    fun getMostRelevantQrCodeIndex(vaccinations: List<QrCodeData.European.Vaccination>): Int
}

class MultipleQrCodesUtilImpl : MultipleQrCodesUtil {

    override fun getMostRelevantQrCodeIndex(vaccinations: List<QrCodeData.European.Vaccination>): Int {
        val mostRelevantVaccination = vaccinations.sortedWith(
            compareBy<QrCodeData.European.Vaccination> { it.dose == "2" && it.ofTotalDoses == "2" && !it.isHidden }
                .thenBy { it.dose == it.ofTotalDoses && !it.isHidden }
                .thenBy { it.dose }
        ).last()
        return vaccinations.indexOfFirst { it == mostRelevantVaccination }
    }
}