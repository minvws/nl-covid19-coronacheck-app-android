package nl.rijksoverheid.ctr.shared.models

data class PersonalDetails(
    val firstNameInitial: String,
    val lastNameInitial: String,
    val birthDay: String,
    val birthMonth: String
)