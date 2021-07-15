package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PaperProofCodeResult: Parcelable {
    @Parcelize
    object None: PaperProofCodeResult(), Parcelable

    @Parcelize
    class Valid(val code: String): PaperProofCodeResult(), Parcelable

    @Parcelize
    object Invalid: PaperProofCodeResult(), Parcelable

    @Parcelize
    object NotSixCharacters: PaperProofCodeResult(), Parcelable
}