package nl.rijksoverheid.ctr.api.models

import com.squareup.moshi.JsonClass

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
data class CoronaCheckErrorResponse(
    val status: String,
    val code: Int
)
