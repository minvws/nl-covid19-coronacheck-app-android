/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

sealed class RemoteCouplingStatus(val typeString: String) {

    companion object {
        const val TYPE_ACCEPTED = "accepted"
        const val TYPE_REJECTED = "rejected"
        const val TYPE_BLOCKED = "blocked"
        const val TYPE_EXPIRED = "expired"
    }

    object Accepted: RemoteCouplingStatus(TYPE_ACCEPTED)
    object Rejected: RemoteCouplingStatus(TYPE_REJECTED)
    object Blocked: RemoteCouplingStatus(TYPE_BLOCKED)
    object Expired: RemoteCouplingStatus(TYPE_EXPIRED)
}