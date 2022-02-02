package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.PaperProofCodeResult

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