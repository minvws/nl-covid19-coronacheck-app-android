package nl.rijksoverheid.ctr.shared.utils.factories

import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactoryImpl
import nl.rijksoverheid.ctr.shared.models.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.lang.IllegalStateException

class ErrorCodeStringFactoryImplTest {

    private val factory = ErrorCodeStringFactoryImpl()

    @Test
    fun `get() returns correct string if errorResult is an IllegalStateException`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResult = AppErrorResult(
                step = Step(1),
                e = IllegalStateException()
            )
        )

        assertEquals(errorCodeString, "A 0 1 000 999")
    }

    @Test
    fun `get() returns correct string if errorResult is a CoronaCheckHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResult = NetworkRequestResult.Failed.CoronaCheckHttpError<Any>(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                )
            )
        )

        assertEquals(errorCodeString, "A 0 1 000 400")
    }

    @Test
    fun `get() returns correct string if errorResult is a ProviderHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResult = NetworkRequestResult.Failed.ProviderHttpError<Any>(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                ),
                provider = "GGD"
            )
        )

        assertEquals(errorCodeString, "A 0 1 GGD 400")
    }

    @Test
    fun `get() returns correct string if errorResult is a CoronaCheckWithErrorResponseHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResult = NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError<Any>(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                ),
                errorResponse = CoronaCheckErrorResponse(
                    status = "",
                    code = 2
                )
            )
        )

        assertEquals(errorCodeString, "A 0 1 000 400 2")
    }
}