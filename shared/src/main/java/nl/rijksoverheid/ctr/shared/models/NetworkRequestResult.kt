package nl.rijksoverheid.ctr.shared.models
import retrofit2.HttpException

/**
 * Base class that should be returned from all repository methods that do network requests
 */
sealed class NetworkRequestResult<R> {

    data class Success<R>(val response: R): NetworkRequestResult<R>()

    sealed class Failed<R>(open val step: Step, open val e: Exception): NetworkRequestResult<R>(), ErrorResult {
        open class CoronaCheckHttpError<R>(override val step: Step, override val e: HttpException): Failed<R>(step, e) {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class ProviderHttpError<R>(override val step: Step, override val e: HttpException, val provider: String): CoronaCheckHttpError<R>(step, e)

        data class CoronaCheckWithErrorResponseHttpError<R>(override val step: Step, override val e: HttpException, val errorResponse: CoronaCheckErrorResponse): CoronaCheckHttpError<R>(step, e) {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class NetworkError<R>(override val step: Step, override val e: Exception): Failed<R>(step, e) {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class Error<R>(override val step: Step, override val e: Exception): Failed<R>(step, e) {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
               return e
            }
        }
    }
}
