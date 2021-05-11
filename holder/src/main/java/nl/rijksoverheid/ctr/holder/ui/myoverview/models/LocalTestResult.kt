package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class LocalTestResult(
    val credentials: String,
    val sampleDate: OffsetDateTime,
    val expireDate: OffsetDateTime,
    val testType: String,
    val personalDetails: PersonalDetails
)
