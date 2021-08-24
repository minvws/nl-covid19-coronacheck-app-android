package nl.rijksoverheid.ctr.shared.models

abstract class Result {
    data class Error(val errorResult: ErrorResult)
    abstract fun getErrorResult(): Error
}