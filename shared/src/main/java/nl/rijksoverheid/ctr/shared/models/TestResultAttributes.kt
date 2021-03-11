package nl.rijksoverheid.ctr.shared.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class TestResultAttributes(
    val sampleTime: Long,
    val testType: String,
    val birthDay: String,
    val birthMonth: String,
    val firstNameInitial: String,
    val lastNameInitial: String
)
