package nl.rijksoverheid.ctr.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class TestResults(
    @Json(name = "test_signatures") val testSignatures: List<TestSignature>,
    @Json(name = "test_results") val testResults: List<TestResult>
) {

    @JsonClass(generateAdapter = true)
    data class TestSignature(
        val uuid: String,
        val signature: String
    )

    @JsonClass(generateAdapter = true)
    data class TestResult(
        val uuid: String,
        @Json(name = "test_type") val testType: String,
        @Json(name = "date_taken") val dateTaken: Long,
        val result: Int
    ) : JSON()
}
