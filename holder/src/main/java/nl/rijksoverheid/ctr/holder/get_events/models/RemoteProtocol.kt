/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class RemoteProtocol(
    val providerIdentifier: String,
    val protocolVersion: String,
    val status: Status,
    val holder: Holder?,
    val events: List<RemoteEvent>?): Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Holder(
        val infix: String?,
        val firstName: String?,
        val lastName: String?,
        val birthDate: String?
    ) : Parcelable

    fun hasEvents(): Boolean {
        return events?.isNotEmpty() ?: false
    }

    enum class Status(val apiStatus: String) {
        UNKNOWN(""),
        PENDING("pending"),
        INVALID_TOKEN("invalid_token"),
        VERIFICATION_REQUIRED("verification_required"),
        RESULT_BLOCKED("result_blocked"),
        COMPLETE("complete");

        companion object {
            fun fromValue(value: String?): Status {
                return values().firstOrNull { it.apiStatus == value } ?: UNKNOWN
            }
        }
    }
}
