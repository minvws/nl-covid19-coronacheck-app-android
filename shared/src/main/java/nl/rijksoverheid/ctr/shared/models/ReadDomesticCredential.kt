package nl.rijksoverheid.ctr.shared.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReadDomesticCredential(
    val birthDay: String,
    val birthMonth: String,
    val credentialVersion: String,
    val firstNameInitial: String,
    val isSpecimen: String,
    val lastNameInitial: String,
    val isPaperProof: String,
    val validForHours: String,
    val validFrom: String
)