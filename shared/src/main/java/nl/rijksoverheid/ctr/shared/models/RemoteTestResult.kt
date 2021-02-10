package nl.rijksoverheid.ctr.shared.models

import com.squareup.moshi.JsonClass
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteTestResult(
    val result: Result,
    val protocolVersion: String,
    val providerIdentifier: String,
    val status: String
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class Result(
        val unique: String,
        val sampleDate: OffsetDateTime,
        val testType: String,
        val negativeResult: Boolean
    )
}
