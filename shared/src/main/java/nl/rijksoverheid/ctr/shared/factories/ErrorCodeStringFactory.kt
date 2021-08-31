package nl.rijksoverheid.ctr.shared.factories

import nl.rijksoverheid.ctr.shared.exceptions.CreateCommitmentMessageException
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import retrofit2.HttpException
import java.lang.StringBuilder

/**
 * Generates a String that we can show in the app to point out want went wrong where
 */
interface ErrorCodeStringFactory {
    fun get(flow: Flow, errorResults: List<ErrorResult>): String
}

class ErrorCodeStringFactoryImpl: ErrorCodeStringFactory {
    override fun get(flow: Flow, errorResults: List<ErrorResult>): String {
        val errorStringBuilders = errorResults.map {
            val stringBuilder = StringBuilder()
            stringBuilder.append("A")
            stringBuilder.append(" ${flow.code}")
            stringBuilder.append(" ${errorResults.first().getCurrentStep().code}")

            if (it is NetworkRequestResult.Failed.ProviderHttpError<*>) {
                stringBuilder.append(" ${it.provider}")
            } else {
                stringBuilder.append(" 000")
            }

            val exceptionErrorCode = when (val exception = it.getException()) {
                is HttpException -> exception.code()
                is CreateCommitmentMessageException -> "054"
                else -> "999"
            }

            stringBuilder.append(" $exceptionErrorCode")

            if (it is NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError<*>) {
                stringBuilder.append(" ${it.errorResponse.code}")
            }

            stringBuilder.toString()
        }

        val errorStringBuilder = StringBuilder()
        errorStringBuilders
            .forEachIndexed { index, string ->
                errorStringBuilder.append(string)
                if (index != errorStringBuilders.size - 1) {
                    errorStringBuilder.append("<br/>")
                }
            }

        return errorStringBuilder.toString()
    }
}