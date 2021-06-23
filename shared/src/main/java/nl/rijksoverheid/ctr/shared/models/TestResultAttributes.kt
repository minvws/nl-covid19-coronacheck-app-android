package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class TestResultAttributes(
    val birthDay: String,
    val birthMonth: String,
    val credentialVersion: String,
    val firstNameInitial: String,
    val lastNameInitial: String,
    val isNLDCC: String?,
    val isSpecimen: String,
    val isPaperProof: String?,
    val validForHours: String?,
    val validFrom: String?
) : Parcelable
