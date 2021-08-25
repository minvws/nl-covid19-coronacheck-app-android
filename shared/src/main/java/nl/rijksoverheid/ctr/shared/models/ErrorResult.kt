package nl.rijksoverheid.ctr.shared.models

interface ErrorResult {
    fun getCurrentStep(): Step
    fun getException(): Exception
}
