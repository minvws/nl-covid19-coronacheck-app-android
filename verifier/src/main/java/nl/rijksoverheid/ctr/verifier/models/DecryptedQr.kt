package nl.rijksoverheid.ctr.verifier.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class DecryptedQr(
    val creationDate: OffsetDateTime,
    val sampleDate: OffsetDateTime,
    val testType: String,
    val firstNameInitial: String,
    val lastNameInitial: String,
    val birthDay: String,
    val birthMonth: String,
    val isPaperProof: String
) : Parcelable
