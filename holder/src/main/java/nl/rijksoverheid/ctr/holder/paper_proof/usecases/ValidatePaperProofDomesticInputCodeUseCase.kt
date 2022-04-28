/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticCodeResult

interface ValidatePaperProofDomesticInputCodeUseCase {
    companion object {
        const val CODE_POINTS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    }

    fun validate(code: String): PaperProofDomesticCodeResult
}

class ValidatePaperProofDomesticInputCodeUseCaseImpl: ValidatePaperProofDomesticInputCodeUseCase {

    override fun validate(code: String): PaperProofDomesticCodeResult {
        code.toCharArray().forEach {
            if (!ValidatePaperProofDomesticInputCodeUseCase.CODE_POINTS.contains(it)) {
                return PaperProofDomesticCodeResult.Invalid
            }
        }

        return when (code.length) {
            0 -> PaperProofDomesticCodeResult.Empty
            6 -> PaperProofDomesticCodeResult.Valid
            else -> PaperProofDomesticCodeResult.Invalid
        }
    }
}