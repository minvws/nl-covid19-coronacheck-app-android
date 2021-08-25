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
/**
 * Response error body that the CoronaCheck API can return
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class CoronaCheckErrorResponse(
    val status: String,
    val code: Int
): Parcelable
