package nl.rijksoverheid.ctr.design.utils

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

sealed class DialogButtonData(open val textId: Int) : Parcelable {

    @Parcelize
    data class NavigationButton(
        override val textId: Int,
        @IdRes val navigationActionId: Int,
        val navigationArguments: Bundle? = null
    ) :
        DialogButtonData(textId), Parcelable

    @Parcelize
    data class ResetApp(override val textId: Int) :
        DialogButtonData(textId), Parcelable

    @Parcelize
    data class Dismiss(override val textId: Int) :
            DialogButtonData(textId), Parcelable

    @Parcelize
    data class NavigateUp(override val textId: Int) :
    DialogButtonData(textId), Parcelable
}
