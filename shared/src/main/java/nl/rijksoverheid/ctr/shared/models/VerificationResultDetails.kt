package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VerificationResultDetails(
    val birthDay: String,
    val birthMonth: String,
    val firstNameInitial: String,
    val lastNameInitial: String,
    val isSpecimen: String,
    val credentialVersion: String,
    val issuerCountryCode: String
) : Parcelable {
    fun isInternationalDCC(): Boolean {
        return issuerCountryCode.isNotEmpty() && issuerCountryCode != "NL"
    }
}