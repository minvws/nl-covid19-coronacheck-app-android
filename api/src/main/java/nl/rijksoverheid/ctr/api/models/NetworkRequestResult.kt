package nl.rijksoverheid.ctr.api.models

import retrofit2.HttpException

sealed class NetworkRequestResult<R>(open val code: Step) {
    data class Success<R>(override val code: Step, val response: R): NetworkRequestResult<R>(code)
    sealed class Failed<R>(override val code: Step): NetworkRequestResult<R>(code) {
        data class HttpError<R>(override val code: Step, val e: HttpException): Failed<R>(code)
        data class CoronaCheckHttpError<R>(override val code: Step, val errorResponse: CoronaCheckErrorResponse): Failed<R>(code)
        data class NetworkError<R>(override val code: Step, val e: Exception): Failed<R>(code)
        data class Error<R>(override val code: Step, val e: Exception): Failed<R>(code)
    }
}
