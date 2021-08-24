package nl.rijksoverheid.ctr.shared.models

import nl.rijksoverheid.ctr.shared.error.Step

interface ErrorResult {
    fun getCurrentStep(): Step
    fun getException(): Exception
}
