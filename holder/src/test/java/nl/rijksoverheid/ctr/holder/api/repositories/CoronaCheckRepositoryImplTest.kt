package nl.rijksoverheid.ctr.holder.api.repositories

import android.util.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.holder.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.RemoteConfigApiClient
import nl.rijksoverheid.ctr.holder.api.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.api.post.GetCredentialsPostData
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingResponse
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.your_events.models.RemotePrepareIssue
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class CoronaCheckRepositoryImplTest : AutoCloseKoinTest() {
    private val base64EncodedCertificate =
        "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUlPVENDQmlHZ0F3SUJBZ0lVZW5nWlRpUmw4am8wZE9nV3djZ1ZmL3p0NEhzd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1NURUxNQWtHQTFVRUJoTUNUa3d4RVRBUEJnTlZCQW9NQ0V0UVRpQkNMbFl1TVNjd0pRWURWUVFEREI1TApVRTRnVUV0SmIzWmxjbWhsYVdRZ1UyVnlkbVZ5SUVOQklESXdNakF3SGhjTk1qRXhNakl5TURrMU1EQXlXaGNOCk1qSXdPVEU0TURrMU1EQXlXakNCZ3pFTE1Ba0dBMVVFQmhNQ1Rrd3hGakFVQmdOVkJBY01EU2R6TFVkeVlYWmwKYm1oaFoyVXhPVEEzQmdOVkJBb01NRTFwYm1semRHVnlhV1VnZG1GdUlGWnZiR3R6WjJWNmIyNWthR1ZwWkN3ZwpWMlZzZW1scWJpQmxiaUJUY0c5eWRERWhNQjhHQTFVRUF3d1lZWEJwTFhSbGMzUXVZMjl5YjI1aGRHVnpkR1Z5CkxtNXNNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQTZadlJOSGlQMXFUbWVnN0gKc3BiRVlqU0lncDdQNXlCUHFrN1N5UnFsdXhoS3F4amVmT2NmRUZoYklEUHJLNU9FZXpLYUFpdktBaHpBN1E2MwplaTk2S1N2RE9WRUs5VjVIZzFDb0xwVXBJdnkrdjljYWtYS1BBLzVnYS9mZzBWWGtPTFl1Q2g1ZndpYkdJOTZlCk5LVFBvWU5XZG53SHdtWGVVOEhreHRrbGRRNDBxMGU2bHRacU1GZm5EdWs5WEpLeU1xS1ZKSER0T1ZyaWIwYm8KSnBRaXFuT0pONW00a1lyUjVuMnIyd0ZIWGtVUGhyMkFkN0dIMStBOHRCQ1RXSm1VeFg5VW44SEJqNDRGbnEzNQpUYWZ0dVErbWVkS3VTblJkVGZxem1acHhTRm1FRzFTbXAwbHYxQnpCcUJkNE53eXk4VFpITHdrc2s1ZmY3VDZyCi80cFhYUUlEQVFBQm80SUQzRENDQTlnd0RBWURWUjBUQVFIL0JBSXdBREFmQmdOVkhTTUVHREFXZ0JRSVNxcTcKbVNSdnZsc0g4YVdLbVZzdFIrKzVQRENCaVFZSUt3WUJCUVVIQVFFRWZUQjdNRTBHQ0NzR0FRVUZCekFDaGtGbwpkSFJ3T2k4IHZZMlZ5ZEM1dFlXNWhaMlZrY0d0cExtTnZiUzlEUVdObGNuUnpMMHRRVGxCTFNXOTJaWEpvWldsawpVMlZ5ZG1WeVEwRXlNREl3TG1ObGNqQXFCZ2dyQmdFRkJRY3dBWVllYUhSMGNEb3ZMMjlqYzNBeU1ESXdMbTFoCmJtRm5aV1J3YTJrdVkyOXRNQ01HQTFVZEVRUWNNQnFDR0dGd2FTMTBaWE4wTG1OdmNtOXVZWFJsYzNSbGNpNXUKYkRCY0JnTlZIU0FFVlRCVE1BZ0dCbWVCREFFQ0FqQkhCZ3BnaEJBQmgyc0JBZ1VKTURrd053WUlLd1lCQlFVSApBZ0VXSzJoMGRIQnpPaTh2WTJWeWRHbG1hV05oWVhRdWEzQnVMbU52YlM5d2EybHZkbVZ5YUdWcFpDOWpjSE13CkhRWURWUjBsQkJZd0ZBWUlLd1lCQlFVSEF3SUdDQ3NHQVFVRkJ3TUJNRk1HQTFVZEh3Uk1NRW93U0tCR29FU0cKUW1oMGRIQTZMeTlqY213dWJXRnVZV2RsWkhCcmFTNWpiMjB2UzFCT1VFdEpiM1psY21obGFXUlRaWEoyWlhKRApRVEl3TWpBdlRHRjBaWE4wUTFKTUxtTnliREFkQmdOVkhRNEVGZ1FVbEZTWFQvcGYwc2lGT0crU2tBVEtmbUZOCkRSY3dEZ1lEVlIwUEFRSC9CQVFEQWdXZ01JSUI4d1lLS3dZQkJBSFdlUUlFQWdTQ0FlTUVnZ0hmQWQwQWRRQkcKcFZYcmRmcVJJREMxb29scDlQTjlFU3hCZEw3OVNiaUZxL0w4Y1A1dFJ3QUFBWDNoakVEVEFBQUVBd0JHTUVRQwpJQ1J1cGx3SGN4R3JpR1BrQlkycXh1aTdUVnkxOStyUmRzQ3VmczJKYUlzdkFpQi9RS3lCS0xCczB2MTJmZ2xSCnBTNlJRRytLKzd2cURUZXp3MFc5cHhjTFlRQjFBRUhJeXJIZklrWktFTWFoT2dsQ2gxNU9NWXNiQSt2clM4ZG8KOEpCaWxnYjJBQUFCZmVHTVFUUUFBQVFEQUVZd1JBSWdIc0RUdEUvc2pLWnNiVG9YTExhRUQ5ZmxsOHZLZUZsOQowTVA1Y3MrcksvWUNJR05DdHd2c2hlSCtPdUlXTnJ4a2tkbTkxdXFZMlFHYjJaNFVmZ3FwOW9iMEFIWUFWWUhVCndoYVFOZ0ZLNmd1YlZ6eFQ4TURrT0hod0pRZ1hMNk9xSFFjVDB3d0FBQUY5IDRZeENIUUFBQkFNQVJ6QkZBaUFDCnF5dzhyaGhnSE1GUlhWMDZsTjJVbytZQk1SenFuYlBTTENuVDJ6OXVSZ0loQUlTN1FjOWh2d0s3WU94bjZBcTIKa1ROOW44WWJLOEpGUDBxeFF5NllCWUhXQUhVQWIxTjJyREh3TVJuWW1RQ2tVUlgvZHhVY0Vka0N3UUFwQm8yeQpDSm8zMlJNQUFBRjk0WXhCRlFBQUJBTUFSakJFQWlBUXJYRzJJbEZHa1VpM3JlVEhuUHorUHhIRHNDcklsdTZaCmVuVE5mR1VCS0FJZ1FqZDZtL1BJL2F0c2YvVlgvbGRHWm5QUWE1cklMVnlZNWRtNGYveWpZb0F3RFFZSktvWkkKaHZjTkFRRUxCUUFEZ2dJQkFNWXdnekVabndjSjNzNmVpTVJtc1hhUExsV1RIaUJmUmxTTjNmdE9tQWFCaXVBVQpKZmZMeFpPSWM1WUtYalB2WkNKS1ZGR1h6ajhqeVdnQ1d2eE5jSnlrUEtXTjRaMTQrdjlSeHVBUW40V2UrOUFICml1TFp0cFpXY0h5OWNOMkU2WTM5d25MVkZ0dHg2cFVhS3ZwaS9FYmdGK2FzMklpbTB6elV2YTgvL2cvR1psb2oKMlpnZE9qdFFXdHhSNytTTnRaTWVyUUl4NnUwVHd2eVRRNncxUVg0OEJrK3FaV05QU1Q3T0syQW1aSUhDcWxkVQpUVElQVEpDVEV3UzNUdHV6dnFqbWRyMU1oMnhtTWFUNUMxcTlZamNrbjhnTm5aKytWN3NmNEpRY1kyVWRrQ204CmhDS0ZuZ2N3a1UwQUorTjI2WVBHVTBuMVJ0MDBmQXBkOCtzaEZtdU1obWFVUnhCRHRWMGNHVE1aK2pRTWFqT0IKRVFvVTRpbXdtMjkrTzRIckdCcGYzc1VDenlVa2pDYVZJT0JnNUk1NTllVmNqZk9XcDZnZzZjc3NuaDJ6ajRhMwpmOGdtdEJVZjJLV1A3NkhqUTc0ZnNKNlN4VU51WERDWVJNNHNna3V6QVNRSkpTbzVNU1dWRDRHTEw3bVlyb01TCm9QNkNEYk54NHN1bE1neTJ4Z0ZxK3JhaHBDN1A4VEZqWjFxWm5rZm1HYWtKbzZrNEVhcVdadFNtRjhJT25meUQKKzl5UGMxQ0pyZ09tbW5pZU5uRFR5WTBMKzRBbjdiOWE4ekpmblZKRnlqalRmUU0rcmFUTzBGRGU2cXZxWFdpdgp2R0wxTU5QaC9WSjB6IEpjM201WlpsSGVNZUEzN0wxcCtJbnE1VHhFK0wxNmNvd0N1UjBnd2xOSktnbkROCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase = mockk()
    private val holderApiClientUtil: HolderApiClientUtil = mockk()
    private val remoteConfigApiClient: RemoteConfigApiClient = mockk(relaxed = true)
    private val networkRequestResultFactory: NetworkRequestResultFactory = mockk()
    private val errorResponseBodyConverter: Converter<ResponseBody, CoronaCheckErrorResponse> =
        mockk()
    private val responseBodyConverter: Converter<ResponseBody, RemoteGreenCards> = mockk()
    private val coronaCheckRepository: CoronaCheckRepository = CoronaCheckRepositoryImpl(
        cachedAppConfigUseCase,
        holderApiClientUtil,
        remoteConfigApiClient,
        errorResponseBodyConverter,
        responseBodyConverter,
        networkRequestResultFactory
    )
    private val holderApiClient: HolderApiClient = mockk(relaxed = true)
    private val certSlot = slot<List<ByteArray>>()

    @Before
    fun setup() {
        coEvery { cachedAppConfigUseCase.getCachedAppConfig().backendTLSCertificates } returns listOf(
            base64EncodedCertificate
        )
        coEvery { holderApiClientUtil.client(any()) } returns holderApiClient
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R : Any> mockRequestResult(
        httpException: HttpException? = null
    ) {
        coEvery {
            networkRequestResultFactory.createResult(
                step = any(),
                provider = any(),
                interceptHttpError = any<suspend (HttpException) -> R>(),
                networkCall = any()
            )
        } coAnswers {
            NetworkRequestResult.Success(
                if (httpException != null) {
                    val interceptHttpError = args[2] as suspend (e: HttpException) -> R
                    interceptHttpError.invoke(httpException)
                } else {
                    val networkCall = args[3] as suspend () -> R
                    networkCall.invoke()
                }
            )
        }
    }

    @Test
    fun `no pinning on tls calls to config providers`() = runBlocking {
        mockRequestResult<RemoteConfigProviders>()

        coronaCheckRepository.configProviders()

        coVerify(exactly = 1) { remoteConfigApiClient.getConfigCtp() }
        coVerify(exactly = 0) { holderApiClientUtil.client(any()) }
    }

    @Test
    fun `pin with config certificates the access tokens request`() = runBlocking {
        mockRequestResult<RemoteAccessTokens>()

        coronaCheckRepository.accessTokens("jwt")

        coVerify(exactly = 1) { holderApiClient.getAccessTokens("Bearer jwt") }
        coVerify(exactly = 1) { holderApiClientUtil.client(capture(certSlot)) }
        assertEquals(base64EncodedCertificate, String(certSlot.captured.first()))
    }

    @Test
    fun `pin with config certificates the get credentials request`() = runBlocking {
        mockRequestResult<RemoteGreenCards>()
        val getCredentialsPostDataSlot = slot<GetCredentialsPostData>()

        coronaCheckRepository.getGreenCards(
            "stoken",
            listOf("event"),
            "issueCommitmentMessage",
            Flow(0)
        )

        coVerify(exactly = 1) { holderApiClient.getCredentials(capture(getCredentialsPostDataSlot)) }
        coVerify(exactly = 1) { holderApiClientUtil.client(capture(certSlot)) }
        assertEquals(base64EncodedCertificate, String(certSlot.captured.first()))
        assertEquals("stoken", getCredentialsPostDataSlot.captured.stoken)
        assertEquals("event", getCredentialsPostDataSlot.captured.events.first())
        assertEquals(
            "issueCommitmentMessage",
            String(
                Base64.decode(
                    getCredentialsPostDataSlot.captured.issueCommitmentMessage,
                    Base64.NO_WRAP
                )
            )
        )
    }

    @Test
    fun `pin with config certificates the prepare issue request`() = runBlocking {
        mockRequestResult<RemotePrepareIssue>()

        coronaCheckRepository.getPrepareIssue()

        coVerify(exactly = 1) { holderApiClient.getPrepareIssue() }
        coVerify(exactly = 1) { holderApiClientUtil.client(capture(certSlot)) }
        assertEquals(base64EncodedCertificate, String(certSlot.captured.first()))
    }

    @Test
    fun `pin with config certificates the coupling request`() = runBlocking {
        mockRequestResult<RemoteCouplingResponse>()
        val getCouplingDataSlot = slot<GetCouplingData>()

        coronaCheckRepository.getCoupling("credential", "couplingCode")

        coVerify(exactly = 1) { holderApiClient.getCoupling(capture(getCouplingDataSlot)) }
        coVerify(exactly = 1) { holderApiClientUtil.client(capture(certSlot)) }
        assertEquals(base64EncodedCertificate, String(certSlot.captured.first()))
        assertEquals("credential", getCouplingDataSlot.captured.credential)
        assertEquals("couplingCode", getCouplingDataSlot.captured.couplingCode)
    }

    @Test
    fun `when get_credentials request fails with error code 99790 then return matching blob ids`() =
        runTest {
            val matchingBlobIds = RemoteGreenCards(
                null, null, RemoteGreenCards.Context(
                    matchingBlobIds = listOf(listOf(1), listOf(2))
                )
            )
            val errorBody = mockk<ResponseBody>(relaxed = true)

            val response = mockk<Response<CoronaCheckErrorResponse>> {
                coEvery { code() } returns 400
                coEvery { message() } returns ""
                coEvery { errorBody() } returns errorBody
            }
            coEvery { errorResponseBodyConverter.convert(errorBody) } returns CoronaCheckErrorResponse(
                "error",
                99790
            )
            coEvery { responseBodyConverter.convert(any()) } returns matchingBlobIds
            mockRequestResult<RemoteGreenCards>(HttpException(response))

            val testResult =
                coronaCheckRepository.getGreenCards("", listOf(), "", HolderFlow.Vaccination)

            assertEquals(matchingBlobIds, (testResult as NetworkRequestResult.Success).response)
        }

    @Test
    fun `when get_credentials request fails with error code other than 99790 then return null`() =
        runTest {
            val errorBody = mockk<ResponseBody>()
            coEvery { errorBody.source().buffer.clone() } returns mockk()

            val response = mockk<Response<CoronaCheckErrorResponse>> {
                coEvery { code() } returns 400
                coEvery { message() } returns ""
                coEvery { errorBody() } returns errorBody
            }
            coEvery { errorResponseBodyConverter.convert(errorBody) } returns CoronaCheckErrorResponse(
                "error",
                99552
            )
            mockRequestResult<RemoteGreenCards>(HttpException(response))

            val testResult =
                coronaCheckRepository.getGreenCards("", listOf(), "", HolderFlow.Vaccination)

            assertNull((testResult as NetworkRequestResult.Success).response)
        }
}
