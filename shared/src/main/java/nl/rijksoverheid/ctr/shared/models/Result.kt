package nl.rijksoverheid.ctr.shared.models

sealed class Result<out T> {
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Failed(val e: Exception?) : Result<Nothing>()
}
