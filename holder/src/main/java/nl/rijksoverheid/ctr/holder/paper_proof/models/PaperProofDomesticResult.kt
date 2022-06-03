/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class PaperProofDomesticResult {
    data class Valid(
        val remoteEvent: RemoteProtocol,
        val eventGroupJsonData: ByteArray
    ) : PaperProofDomesticResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Valid

            if (remoteEvent != other.remoteEvent) return false
            if (!eventGroupJsonData.contentEquals(other.eventGroupJsonData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = remoteEvent.hashCode()
            result = 31 * result + eventGroupJsonData.contentHashCode()
            return result
        }
    }

    sealed class Invalid : PaperProofDomesticResult() {
        object ExpiredQr : Invalid()
        object RejectedQr : Invalid()
        object BlockedQr : Invalid()
        data class Error(val errorResult: ErrorResult) : Invalid()
    }
}