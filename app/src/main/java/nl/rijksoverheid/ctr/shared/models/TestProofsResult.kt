package nl.rijksoverheid.ctr.shared.models

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
data class TestProofsResult(
    @Json(name = "test_proofs") val testProofs: List<TestProofs>,
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class TestProofs(
        @Json(name = "test_proof") val testProof: TestProof,
        val signature: String,
        @Json(name = "test_type") val testType: TestType
    )

    @JsonClass(generateAdapter = true)
    data class TestProof(
        val uuid: String,
        @Json(name = "ism") val ismBase64: String,
        val attributes: List<String>
    ) : JSON()

    @JsonClass(generateAdapter = true)
    data class TestType(
        val uuid: String,
        val name: String
    )
}
