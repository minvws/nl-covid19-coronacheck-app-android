/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofCodeResult

interface PaperProofCodeUseCase {
    companion object {
        const val CODE_POINTS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        const val CHARS_COUNT = 6
    }

    fun validate(code: String): PaperProofCodeResult
}

class PaperProofCodeUseCaseImpl: PaperProofCodeUseCase {

    override fun validate(code: String): PaperProofCodeResult {
        code.toCharArray().forEach {
            if (!PaperProofCodeUseCase.CODE_POINTS.contains(it)) {
                return PaperProofCodeResult.Invalid
            }
        }

        return when (code.length) {
            0 -> PaperProofCodeResult.Empty
            6 -> PaperProofCodeResult.Valid
            else -> PaperProofCodeResult.Invalid
        }
    }
}