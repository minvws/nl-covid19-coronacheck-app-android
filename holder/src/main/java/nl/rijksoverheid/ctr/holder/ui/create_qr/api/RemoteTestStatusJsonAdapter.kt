/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2

class RemoteTestStatusJsonAdapter {
    @FromJson
    fun fromJson(value: String?): RemoteTestResult2.Status = RemoteTestResult2.Status.fromValue(value)

    @ToJson
    fun toJson(value: RemoteTestResult2.Status): String = value.apiStatus
}
