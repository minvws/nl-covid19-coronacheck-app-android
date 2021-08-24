package nl.rijksoverheid.ctr.shared.models
import nl.rijksoverheid.ctr.shared.error.Step
import retrofit2.HttpException
import java.io.Serializable

sealed class NetworkRequestResult<R> {

    data class Success<R>(val response: R): NetworkRequestResult<R>(), Serializable

    sealed class Failed<R>(open val step: Step, open val e: Exception): NetworkRequestResult<R>(), ErrorResult, Serializable {
        open class HttpError<R>(override val step: Step, override val e: HttpException): Failed<R>(step, e), Serializable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class ProviderHttpError<R>(override val step: Step, override val e: HttpException, val provider: String): HttpError<R>(step, e), Serializable

        data class CoronaCheckHttpError<R>(override val step: Step, override val e: HttpException, val errorResponse: CoronaCheckErrorResponse): Failed<R>(step, e), Serializable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class NetworkError<R>(override val step: Step, override val e: Exception): Failed<R>(step, e), Serializable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class Error<R>(override val step: Step, override val e: Exception): Failed<R>(step, e), Serializable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
               return e
            }
        }
    }
}
