/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType

interface ScopeUtil {

    fun getScopeForRemoteOriginType(remoteOriginType: RemoteOriginType,
                 withIncompleteVaccination: Boolean): String?

    fun getScopeForOriginType(originType: OriginType,
                 withIncompleteVaccination: Boolean): String?
}

class ScopeUtilImpl: ScopeUtil {

    override fun getScopeForRemoteOriginType(
        remoteOriginType: RemoteOriginType,
        withIncompleteVaccination: Boolean
    ): String? {
        return getScopeForOriginType(
            originType = remoteOriginType.toOriginType(),
            withIncompleteVaccination = withIncompleteVaccination
        )
    }

    override fun getScopeForOriginType(
        originType: OriginType,
        withIncompleteVaccination: Boolean
    ): String? {
        return if (originType is OriginType.Recovery) {
            return if (withIncompleteVaccination) {
                "firstepisode"
            } else {
                "recovery"
            }
        } else {
            null
        }
    }
}