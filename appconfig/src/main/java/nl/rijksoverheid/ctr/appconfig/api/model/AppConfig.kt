/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api.model

import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.shared.models.JSON

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class AppConfig(
    val appDeactivated: Boolean,
    val informationURL: String,
    val minimumVersion: Int,
    val configTtlSeconds: Int,
    val configMinimumIntervalSeconds: Int,
    val providers: List<Code>,
    val recommendedVersion: Int,
    val recommendedUpgradeIntervalHours: Int,
    val deeplinkDomains: List<Url>,
    val clockDeviationThresholdSeconds: Int,
    val configAlmostOutOfDateWarningSeconds : Int,
) : JSON() {

    @JsonClass(generateAdapter = true)
    data class HpkCode(
        val code: String,
        val name: String,
        val vp: String,
        val mp: String,
        val ma: String
    )

    @JsonClass(generateAdapter = true)
    data class Code(val code: String, val name: String) : JSON()

    @JsonClass(generateAdapter = true)
    data class Url(val url: String, val name: String) : JSON()
}
