package nl.rijksoverheid.ctr.shared.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.HttpException

/**
 * Base class that should be returned from all repository methods that do network requests
 */
sealed class NetworkRequestResult<out R> {

    data class Success<R>(val response: R): NetworkRequestResult<R>()

    sealed class Failed(open val step: Step, open val e: Exception): NetworkRequestResult<Nothing>(), ErrorResult {

        @Parcelize
        open class CoronaCheckHttpError(override val step: Step, override val e: HttpException): Failed(step, e), Parcelable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        @Parcelize
        data class ProviderHttpError(override val step: Step, override val e: HttpException, val provider: String): CoronaCheckHttpError(step, e), Parcelable

        @Parcelize
        data class ProviderError(override val step: Step, override val e: Exception, val provider: String): Error(step, e), Parcelable

        @Parcelize
        data class CoronaCheckWithErrorResponseHttpError(override val step: Step, override val e: HttpException, val errorResponse: CoronaCheckErrorResponse): CoronaCheckHttpError(step, e), Parcelable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }

            fun getCode(): Int {
                return errorResponse.code
            }
        }

        @Parcelize
        data class NetworkError(override val step: Step, override val e: Exception): Failed(step, e), Parcelable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
                return e
            }
        }

        @Parcelize
        open class Error(override val step: Step, override val e: Exception): Failed(step, e), Parcelable {
            override fun getCurrentStep(): Step {
                return step
            }

            override fun getException(): Exception {
               return e
            }
        }
    }
}
