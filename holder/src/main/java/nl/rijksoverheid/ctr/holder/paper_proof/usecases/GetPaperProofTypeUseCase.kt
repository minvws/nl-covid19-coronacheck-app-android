/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDccCountry
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType

interface GetPaperProofTypeUseCase {
    suspend fun get(qrContent: String): PaperProofType
}

class GetPaperProofTypeUseCaseImpl: GetPaperProofTypeUseCase {
    override suspend fun get(qrContent: String): PaperProofType {
        return withContext(Dispatchers.IO) {
            val isDcc = true
            if (isDcc) {
                val isForeign = false
                PaperProofType.DCC(
                    qrContent = qrContent,
                    country = if (isForeign) PaperProofDccCountry.Foreign else PaperProofDccCountry.Dutch
                )
            } else {
                val isCtb = false
                if (isCtb) {
                    PaperProofType.CTB(
                        qrContent = qrContent
                    )
                } else {
                    PaperProofType.Unknown(
                        qrContent = qrContent
                    )
                }
            }
        }
    }
}