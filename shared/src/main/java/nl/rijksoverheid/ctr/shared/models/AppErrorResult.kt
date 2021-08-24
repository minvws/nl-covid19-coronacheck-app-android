package nl.rijksoverheid.ctr.shared.models

import nl.rijksoverheid.ctr.shared.error.Step
import java.io.Serializable

data class AppErrorResult(val step: Step, val e: Exception) : ErrorResult, Serializable {
    override fun getCurrentStep(): Step {
        return step
    }

    override fun getException(): Exception {
        return e
    }
}