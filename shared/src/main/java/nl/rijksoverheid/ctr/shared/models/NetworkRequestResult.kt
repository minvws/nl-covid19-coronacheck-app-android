package nl.rijksoverheid.ctr.shared.models
import nl.rijksoverheid.ctr.shared.error.Step
import retrofit2.HttpException
import java.io.Serializable

sealed class NetworkRequestResult<R> {

    data class Success<R>(val response: R): NetworkRequestResult<R>()

    sealed class Failed<R>(open val step: Step, open val e: Exception): NetworkRequestResult<R>(), ErrorResult {
        open class HttpError<R>(override val step: Step, override val e: HttpException): Failed<R>(step, e) {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        data class ProviderHttpError<R>(override val step: Step, override val e: HttpException, val provider: String): HttpError<R>(step, e)

        data class CoronaCheckHttpError<R>(override val step: Step, override val e: HttpException, val errorResponse: CoronaCheckErrorResponse): HttpError<R>(step, e) {
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
