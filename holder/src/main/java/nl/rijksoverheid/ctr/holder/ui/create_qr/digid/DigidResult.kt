package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class DigidResult {
    data class Success(val jwt: String): DigidResult()
    data class Failed(val errorResult: ErrorResult): DigidResult()
    object Cancelled: DigidResult()
}