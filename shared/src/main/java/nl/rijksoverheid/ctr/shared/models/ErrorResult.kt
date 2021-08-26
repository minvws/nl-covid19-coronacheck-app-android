package nl.rijksoverheid.ctr.shared.models

/**
 * Base class for showing errors in the UI
 */
interface ErrorResult {
    fun getCurrentStep(): Step
    fun getException(): Exception
}
