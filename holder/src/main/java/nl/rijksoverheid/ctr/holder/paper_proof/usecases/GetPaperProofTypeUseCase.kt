/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import org.json.JSONObject

interface GetPaperProofTypeUseCase {
    suspend fun get(qrContent: String): PaperProofType
}

class GetPaperProofTypeUseCaseImpl(
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase,
    private val paperProofUtil: PaperProofUtil
): GetPaperProofTypeUseCase {
    override suspend fun get(qrContent: String): PaperProofType {
        return withContext(Dispatchers.IO) {
            val isDcc = true
            if (isDcc) {
                val isForeign = true
                val event = getEventsFromPaperProofQrUseCase.get(qrContent)

                if (isForeign) {
                    PaperProofType.DCC.Foreign(
                        events = mapOf(event to paperProofUtil.getSignerCredential(qrContent))
                    )
                } else {
                    PaperProofType.DCC.Dutch(
                        qrContent = qrContent
                    )
                }
            } else {
                val isCtb = false
                if (isCtb) {
                    PaperProofType.CTB
                } else {
                    PaperProofType.Unknown
                }
            }
        }
    }
}