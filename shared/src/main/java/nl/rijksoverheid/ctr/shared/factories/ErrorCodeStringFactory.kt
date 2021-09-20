package nl.rijksoverheid.ctr.shared.factories

import android.database.sqlite.SQLiteConstraintException
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import nl.rijksoverheid.ctr.shared.exceptions.CreateCommitmentMessageException
import nl.rijksoverheid.ctr.shared.exceptions.OpenIdAuthorizationException
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import retrofit2.HttpException
import java.lang.StringBuilder
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.CharacterCodingException
import javax.net.ssl.*

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
            stringBuilder.append("${errorResults.first().getCurrentStep().code}")

            if (it is NetworkRequestResult.Failed.ProviderHttpError) {
                stringBuilder.append(" ${it.provider}")
            } else if (it is NetworkRequestResult.Failed.ProviderError) {
                stringBuilder.append(" ${it.provider}")
            } else {
                stringBuilder.append(" 000")
            }

            val exceptionErrorCode = when (val exception = it.getException()) {
                is HttpException -> exception.code()
                is CreateCommitmentMessageException -> "054"
                is JsonEncodingException -> "031"
                is JsonDataException -> "030"
                is SSLHandshakeException -> "010"
                is SSLKeyException -> "011"
                is SSLProtocolException -> "012"
                is SSLPeerUnverifiedException -> "013"
                is SQLiteConstraintException -> "060"
                is OpenIdAuthorizationException -> "07${exception.type}-${exception.code}"
                is CharacterCodingException -> "020"
                is SocketTimeoutException -> "004"
                is UnknownHostException -> "002"
                is ConnectException -> "005"
                else -> throw it.getException()
            }

            stringBuilder.append(" $exceptionErrorCode")

            if (it is NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError) {
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