package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class to represent app (not network) triggered errors
 */
@Parcelize
data class AppErrorResult(val step: Step, val e: Exception) : ErrorResult, Parcelable {
    override fun getCurrentStep(): Step {
        return step
    }

    override fun getException(): Exception {
        return e
    }
}