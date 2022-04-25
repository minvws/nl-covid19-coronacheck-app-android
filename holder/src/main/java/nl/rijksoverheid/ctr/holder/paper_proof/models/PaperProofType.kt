/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

sealed class PaperProofType(open val qrContent: String) {
    data class DCC(val country: PaperProofDccCountry, override val qrContent: String): PaperProofType(qrContent)
    data class CTB(override val qrContent: String) : PaperProofType(qrContent)
    data class Unknown(override val qrContent: String): PaperProofType(qrContent)
}