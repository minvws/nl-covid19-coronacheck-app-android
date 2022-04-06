/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol

class RemoteTestStatusJsonAdapter {
    @FromJson
    fun fromJson(value: String?): RemoteProtocol.Status = RemoteProtocol.Status.fromValue(value)

    @ToJson
    fun toJson(value: RemoteProtocol.Status): String = value.apiStatus
}
