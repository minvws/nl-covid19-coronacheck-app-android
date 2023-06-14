/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.pdf

import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class EUPrintAttributes(
    val dcc: JSONObject,
    val expirationTime: OffsetDateTime,
    val qr: String
)

@JsonClass(generateAdapter = true)
data class PrintAttributes(val european: List<EUPrintAttributes>)
