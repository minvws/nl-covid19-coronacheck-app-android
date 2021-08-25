package nl.rijksoverheid.ctr.shared.error

import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import retrofit2.HttpException
import java.lang.StringBuilder

/**
 * Generates a String that we can show in the app to point out want went wrong where
 */
interface ErrorCodeStringFactory {
    fun get(flow: Flow, errorResult: ErrorResult): String
}

class ErrorCodeStringFactoryImpl: ErrorCodeStringFactory {
    override fun get(flow: Flow, errorResult: ErrorResult): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("A")
        stringBuilder.append(" ${flow.code}")
        stringBuilder.append(" ${errorResult.getCurrentStep().code}")

        if (errorResult is NetworkRequestResult.Failed.ProviderHttpError<*>) {
            stringBuilder.append(" ${errorResult.provider}")
        } else {
            stringBuilder.append(" 000")
        }

        val exceptionErrorCode = when (val exception = errorResult.getException()) {
            is HttpException -> exception.code()
            else -> "999"
        }

        stringBuilder.append(" $exceptionErrorCode")

        if (errorResult is NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError<*>) {
            stringBuilder.append(" ${errorResult.errorResponse.code}")
        }

        return stringBuilder.toString()
    }
}