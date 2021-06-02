package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

sealed class DigidResult {
    data class Success(val jwt: String): DigidResult()
    data class Failed(val error: String?): DigidResult()
}