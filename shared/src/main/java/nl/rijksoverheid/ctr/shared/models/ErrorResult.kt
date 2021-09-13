package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable

/**
 * Base class for showing errors in the UI
 */
interface ErrorResult: Parcelable {
    fun getCurrentStep(): Step
    fun getException(): Exception
}
