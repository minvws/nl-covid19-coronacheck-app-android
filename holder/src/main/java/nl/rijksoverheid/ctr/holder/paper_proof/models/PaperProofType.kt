/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3

sealed class PaperProofType() {
    sealed class DCC(): PaperProofType() {
        data class Foreign(val events: Map<RemoteProtocol3, ByteArray>) : DCC()
        data class Dutch(val qrContent: String) : DCC()
    }
    object CTB: PaperProofType()
    object Unknown: PaperProofType()
}