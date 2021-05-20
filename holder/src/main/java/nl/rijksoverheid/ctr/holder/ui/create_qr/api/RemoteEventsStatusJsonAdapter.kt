/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents

class RemoteEventsStatusJsonAdapter {
    @FromJson
    fun fromJson(value: String?): RemoteEvents.Status = RemoteEvents.Status.fromValue(value)

    @ToJson
    fun toJson(value: RemoteEvents.Status): String = value.apiStatus
}