package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class ErrorResultFragmentData(
    val title: String,
    val description: String,
    val buttonTitle: String,
    val buttonAction: ButtonAction,
    val urlData: UrlData? = null): Parcelable {

    @Parcelize
    data class UrlData(
        val urlButtonTitle: String,
        val urlButtonUrl: String
    ): Parcelable

    sealed class ButtonAction: Parcelable {
        @Parcelize
        data class Destination(@IdRes val buttonDestinationId: Int): ButtonAction(), Parcelable

        @Parcelize
        object PopBackStack: ButtonAction(), Parcelable
    }
}