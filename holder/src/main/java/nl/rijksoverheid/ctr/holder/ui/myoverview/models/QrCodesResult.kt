package nl.rijksoverheid.ctr.holder.ui.myoverview.models

sealed class QrCodesResult {
    data class SingleQrCode(val qrCodeData: QrCodeData) : QrCodesResult()

    // Only european vaccinations support multiple qrs
    data class MultipleQrCodes(
        val europeanVaccinationQrCodeDataList: List<QrCodeData.European.Vaccination>,
        val mostRelevantVaccinationIndex: Int
    ) : QrCodesResult()
}