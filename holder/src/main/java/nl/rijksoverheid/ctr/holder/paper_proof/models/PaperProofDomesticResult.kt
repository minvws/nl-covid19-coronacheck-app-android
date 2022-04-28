/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3
import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class PaperProofDomesticResult {
    data class Valid(val events: Map<RemoteProtocol3, ByteArray>) : PaperProofDomesticResult()
    sealed class Invalid : PaperProofDomesticResult() {
        object ExpiredQr : Invalid()
        object RejectedQr : Invalid()
        object BlockedQr : Invalid()
        data class Error(val errorResult: ErrorResult) : Invalid()
    }
}