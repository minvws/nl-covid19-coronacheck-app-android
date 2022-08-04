package nl.rijksoverheid.ctr.shared.utils.factories

import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactoryImpl
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.MissingOriginException
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.models.Step
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ErrorCodeStringFactoryImplTest {

    private val factory = ErrorCodeStringFactoryImpl()

    @Test
    fun `get() throws error if errorResult is an IllegalStateException`() {
        assertThrows(IllegalStateException::class.java) {
            factory.get(
                flow = Flow(0),
                errorResults = listOf(
                    AppErrorResult(
                        step = Step(1),
                        e = IllegalStateException()
                    )
                )
            )
        }
    }

    @Test
    fun `get() returns correct string if errorResult is a CoronaCheckHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResults = listOf(NetworkRequestResult.Failed.CoronaCheckHttpError(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                ),
                provider = "GGD"
            ))
        )

        assertEquals(errorCodeString, "A 01 GGD 400")
    }

    @Test
    fun `get() returns correct string if errorResult is a ProviderHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResults = listOf(NetworkRequestResult.Failed.ProviderHttpError(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                ),
                provider = "GGD"
            )
        ))

        assertEquals(errorCodeString, "A 01 GGD 400")
    }

    @Test
    fun `get() returns correct string if errorResult is a CoronaCheckWithErrorResponseHttpError`() {
        val errorCodeString = factory.get(
            flow = Flow(0),
            errorResults = listOf(NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError(
                step = Step(1),
                e = HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                ),
                errorResponse = CoronaCheckErrorResponse(
                    status = "",
                    code = 2
                ),
                provider = null
            )
        ))

        assertEquals(errorCodeString, "A 01 000 400 2")
    }

    @Test
    fun `get() returns correct string if errorResult is a OriginMismatchError`() {
        val errorCodeString = factory.get(
            flow = Flow(2),
            errorResults = listOf(object : ErrorResult {
                override fun getCurrentStep(): Step {
                    return Step(80)
                }

                override fun getException(): Exception {
                    return MissingOriginException()
                }
            }))

        assertEquals("A 280 000 058", errorCodeString)
    }
}
