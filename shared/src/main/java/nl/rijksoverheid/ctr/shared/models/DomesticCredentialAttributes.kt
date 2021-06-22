package nl.rijksoverheid.ctr.shared.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class DomesticCredentialAttributes(
    val birthDay: String,
    val birthMonth: String,
    val credentialVersion: Int,
    val firstNameInitial: String,
    val isSpecimen: String,
    val lastNameInitial: String,
    val isPaperProof: String,
    val validForHours: Long,
    val validFrom: Long
)
