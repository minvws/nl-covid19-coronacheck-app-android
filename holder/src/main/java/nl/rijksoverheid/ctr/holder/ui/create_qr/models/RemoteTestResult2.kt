package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteTestResult2(
    override val providerIdentifier: String,
    override val protocolVersion: String,
    override val status: Status,
    val result: Result?,
) : RemoteProtocol(providerIdentifier, protocolVersion, status), Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Result(
        val unique: String,
        val sampleDate: OffsetDateTime,
        val testType: String,
        val negativeResult: Boolean,
        val holder: Holder
    ) : Parcelable

    override fun hasEvents(): Boolean {
        return true
    }
}
