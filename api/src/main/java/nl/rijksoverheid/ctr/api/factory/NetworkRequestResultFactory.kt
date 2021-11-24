package nl.rijksoverheid.ctr.api.factory

import nl.rijksoverheid.ctr.shared.models.*
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * This class should be used for every network request
 */
class NetworkRequestResultFactory(
    private val errorResponseBodyConverter: Converter<ResponseBody, CoronaCheckErrorResponse>,
    private val androidUtil: AndroidUtil,
    private val mijnCnErrorResponseBodyConverter: Converter<ResponseBody, MijnCnErrorResponse>,
) {

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
                    when (it) {
                        "BES" -> {
                            // BES provider returns errors differently from other providers, handle these accordingly
                            // Check if there is a error body
                            val errorBody = httpException.response()?.errorBody()
                                ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException)

                            // Check if the error body is a [MijnCnErrorResponse]
                            val errorResponse = mijnCnErrorResponseBodyConverter.convert(errorBody)
                                ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException)

                            return NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError(
                                step,
                                httpException,
                                CoronaCheckErrorResponse(errorResponse.model.error, errorResponse.model.code)
                            )
                        }
                        else -> {
                            return NetworkRequestResult.Failed.ProviderHttpError(
                                step,
                                httpException,
                                it
                            )
                        }
                    }

                }

                // Check if there is a error body
                val errorBody = httpException.response()?.errorBody()
                    ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException)

                // Check if the error body is a [CoronaCheckErrorResponse]
                val errorResponse = errorResponseBodyConverter.convert(errorBody)
                    ?: return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException)

                return NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError(
                    step,
                    httpException,
                    errorResponse
                )
            } catch (e: Exception) {
                return NetworkRequestResult.Failed.CoronaCheckHttpError(step, httpException)
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