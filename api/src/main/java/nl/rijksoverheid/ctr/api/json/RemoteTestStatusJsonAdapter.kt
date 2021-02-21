/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.api.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.api.models.RemoteTestResult

class RemoteTestStatusJsonAdapter {
    @FromJson
    fun fromJson(value: String?): RemoteTestResult.Status = RemoteTestResult.Status.fromValue(value)

    @ToJson
    fun toJson(value: RemoteTestResult.Status): String = value.apiStatus
}
