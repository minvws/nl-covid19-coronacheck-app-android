package nl.rijksoverheid.ctr.citizen.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.shared.models.JSON
import nl.rijksoverheid.ctr.shared.models.TestResults

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class CitizenQrPayload(
    @Json(name = "event_uuid") val eventUuid: String,
    val time: Long,
    val test: TestResults.TestResult,
    @Json(name = "test_signature") val testSignature: String
) : JSON()
