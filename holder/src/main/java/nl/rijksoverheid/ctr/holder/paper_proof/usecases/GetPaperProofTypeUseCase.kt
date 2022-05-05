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
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface GetPaperProofTypeUseCase {
    suspend fun get(qrContent: String): PaperProofType
}

class GetPaperProofTypeUseCaseImpl(
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase,
    private val paperProofUtil: PaperProofUtil,
    private val mobileCoreWrapper: MobileCoreWrapper
): GetPaperProofTypeUseCase {
    override suspend fun get(qrContent: String): PaperProofType {
        return withContext(Dispatchers.IO) {
            if (mobileCoreWrapper.isDcc(qrContent.toByteArray())) {
                val isForeign = mobileCoreWrapper.isForeignDcc(qrContent.toByteArray())
                val event = getEventsFromPaperProofQrUseCase.get(qrContent)

                if (isForeign) {
                    PaperProofType.DCC.Foreign(
                        remoteProtocol3 = event,
                        eventGroupJsonData = paperProofUtil.getEventGroupJsonData(qrContent = qrContent)
                    )
                } else {
                    PaperProofType.DCC.Dutch(
                        qrContent = qrContent
                    )
                }
            } else {
                val hasDomesticPrefix = mobileCoreWrapper.hasDomesticPrefix(qrContent.toByteArray())
                if (hasDomesticPrefix) {
                    PaperProofType.CTB
                } else {
                    PaperProofType.Unknown
                }
            }
        }
    }
}