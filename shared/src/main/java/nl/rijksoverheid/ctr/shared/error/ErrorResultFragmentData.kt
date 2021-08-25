package nl.rijksoverheid.ctr.shared.error

import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class ErrorResultFragmentData(
    val title: String,
    val description: String,
    val buttonTitle: String,
    val urlData: UrlData? = null,
    @IdRes val buttonDestinationId: Int): Parcelable {

    @Parcelize
    data class UrlData(
        val urlButtonTitle: String,
        val urlButtonUrl: String
    ): Parcelable
}