package nl.rijksoverheid.ctr.api.factory

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.MijnCnErrorResponse
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.models.Step
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException

/**
 * This class should be used for every network request
 */
class NetworkRequestResultFactory(
    private val errorResponseBodyConverter: Converter<ResponseBody, CoronaCheckErrorResponse>,
    private val androidUtil: AndroidUtil,
    private val mijnCnErrorResponseBodyConverter: Converter<ResponseBody, MijnCnErrorResponse>
) {

    private companion object {
        const val BES_PROVIDER = "BES"
    }

    suspend fun <R : Any> createResult(
        step: Step,
        provider: String? = null,
        interceptHttpError: (suspend (e: HttpException) -> R?)? = null,
        networkCall: suspend () -> R
    ): NetworkRequestResult<R> {
        return try {
            if (!androidUtil.isNetworkAvailable()) {
                NetworkRequestResult.Failed.ClientNetworkError(step)
            } else {
                val response = networkCall.invoke()
                NetworkRequestResult.Success(response)
            }
        } catch (httpException: HttpException) {
            try {
                // We intercept here if a HttpException is expected
                val result = interceptHttpError?.invoke(httpException)
                result?.let {
                    return NetworkRequestResult.Success(it)
                }

                provider?.let {
                    // If this is a call to a provider we return a ProviderHttpError
                    return NetworkRequestResult.Failed.ProviderHttpError(
                        step, httpException, it
                    )
                }

                // Check if there is a error body
                val errorBody = httpException.response()?.errorBody()
                    ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException, provider)

                // Check if the error body is a [CoronaCheckErrorResponse]
                val errorResponse = if (provider == BES_PROVIDER) {
                    mijnCnErrorResponseBodyConverter.convert(errorBody)?.let {
                        CoronaCheckErrorResponse(it.model.error, it.model.code)
                    }
                } else {
                    errorResponseBodyConverter.convert(errorBody)
                } ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(
                    step, httpException, provider
                )

                return NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError(
                    step, httpException, errorResponse, provider
                )
            } catch (e: Exception) {
                return NetworkRequestResult.Failed.CoronaCheckHttpError(
                    step, httpException, provider
                )
            }
        } catch (e: IOException) {
            when {
                e is SocketTimeoutException || e is UnknownHostException || e is ConnectException -> {
                    NetworkRequestResult.Failed.ServerNetworkError(step, e)
                }
                provider != null -> NetworkRequestResult.Failed.ProviderError(step, e, provider)
                else -> NetworkRequestResult.Failed.Error(step, e)
            }
        } catch (e: Exception) {
            NetworkRequestResult.Failed.Error(step, e)
        }
    }
}
