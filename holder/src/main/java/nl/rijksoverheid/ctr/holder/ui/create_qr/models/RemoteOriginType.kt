/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/**
 * Types of origins that we communicate with the backend.
 * The difference with [OriginType] is that this is used for sending
 * and [OriginType] for retrieving from backend and database
 */
sealed class RemoteOriginType: Parcelable {

    @Parcelize
    object Vaccination: RemoteOriginType(), Parcelable

    @Parcelize
    object Recovery: RemoteOriginType(), Parcelable

    @Parcelize
    object Test: RemoteOriginType(), Parcelable

    fun toOriginType(): OriginType {
        return when (this) {
            is Vaccination -> OriginType.Vaccination
            is Recovery -> OriginType.Recovery
            is Test -> OriginType.Test
        }
    }
}