/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol

sealed class PaperProofType {
    sealed class DCC: PaperProofType() {
        data class Foreign(
            val remoteProtocol: RemoteProtocol,
            val eventGroupJsonData: ByteArray
        ) : DCC() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Foreign

                if (remoteProtocol != other.remoteProtocol) return false
                if (!eventGroupJsonData.contentEquals(other.eventGroupJsonData)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = remoteProtocol.hashCode()
                result = 31 * result + eventGroupJsonData.contentHashCode()
                return result
            }
        }

        data class Dutch(val qrContent: String) : DCC()
    }
    object CTB: PaperProofType()
    object Unknown: PaperProofType()
}