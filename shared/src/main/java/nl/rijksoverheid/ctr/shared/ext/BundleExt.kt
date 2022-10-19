package nl.rijksoverheid.ctr.shared.ext

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
// Switch over to official androidx versions once https://issuetracker.google.com/issues/242048899
// is fixed.
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        getParcelable(key)
    } else {
        // Note that this can throw NPE in specific cases on Android 13
        // We should be fine though, since the parcelables we use all have a CREATOR inner class
        // See https://issuetracker.google.com/issues/240585930#comment6
        getParcelable(key, T::class.java)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayCompat(key: String): Array<T>? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        getParcelableArray(key)?.map {
            // this will throw if the elements are not of the correct type
            it as T
        }?.toTypedArray()
    } else {
        getParcelableArray(key, T::class.java)
    }
}
