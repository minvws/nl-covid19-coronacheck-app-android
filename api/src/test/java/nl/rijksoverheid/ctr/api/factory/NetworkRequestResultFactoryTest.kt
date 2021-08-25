package nl.rijksoverheid.ctr.api.factory

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.models.Step
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class NetworkRequestResultFactoryTest {

    private val TestStep = Step(1)
    private lateinit var mockWebServer: MockWebServer
    private lateinit var testApi: TestApi
    private lateinit var networkRequestResultFactory: NetworkRequestResultFactory

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder().client(okHttpClient.build())
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
        val converter: Converter<ResponseBody, CoronaCheckErrorResponse> = retrofit.responseBodyConverter(
            CoronaCheckErrorResponse::class.java, emptyArray()
        )
        testApi = retrofit.create(TestApi::class.java)
        networkRequestResultFactory = NetworkRequestResultFactory(converter)
    }

    @Test
    fun `createResult returns Success if request successful`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"hello\":\"world\"}")
                .setResponseCode(200)
        )

        val result = networkRequestResultFactory.createResult(
            TestStep
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Success)
    }

    @Test
    fun `createResult returns HttpError if request failed with normal body`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"hello\":\"world\"}")
                .setResponseCode(404)
        )

        val result = networkRequestResultFactory.createResult(
            TestStep
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Failed.CoronaCheckHttpError)
    }

    @Test
    fun `createResult returns ProviderHttpError if request to provider failed with normal body`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"hello\":\"world\"}")
                .setResponseCode(404)
        )

        val result = networkRequestResultFactory.createResult(
            step = TestStep,
            provider = "GGD"
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Failed.ProviderHttpError)
    }

    @Test
    fun `createResult returns CoronaCheckWithErrorResponseHttpError if request failed with specific body`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"status\":\"1\",\"code\":1000}")
                .setResponseCode(404)
        )

        val result = networkRequestResultFactory.createResult(
            TestStep
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError)
    }

    @Test
    fun `createResult returns NetworkError if request has no response`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"hello\":\"world\"}")
                .setResponseCode(200)
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        val result = networkRequestResultFactory.createResult(
            TestStep
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Failed.NetworkError)
    }

    @Test
    fun `createResult returns Error if failed to parse`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"world\":\"hello\"}")
                .setResponseCode(200)
        )

        val result = networkRequestResultFactory.createResult(
            TestStep
        ) {
            testApi.request()
        }

        assertTrue(result is NetworkRequestResult.Failed.Error)
    }

    interface TestApi {
        @GET("/")
        suspend fun request(): TestObject
    }

    @JsonClass(generateAdapter = true)
    data class TestObject(val hello: String)
}