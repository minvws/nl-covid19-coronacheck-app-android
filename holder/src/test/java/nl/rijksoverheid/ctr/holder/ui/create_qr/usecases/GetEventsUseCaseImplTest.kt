package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GetEventsUseCaseImplTest {
    private val configProvidersUseCase: ConfigProvidersUseCase =  mockk()
    private val coronaCheckRepository: CoronaCheckRepository =  mockk()
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase =  mockk()
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase =  mockk()

    private val eventsError = mockk<NetworkRequestResult.Failed.Error>()
    private val jwt = "jwt"
    private val originType = OriginType.Test
    private val targetProviderIds = listOf(RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_GGD)

    private suspend fun getEvents(): EventsResult {
        val getEventsUseCase = GetEventsUseCaseImpl(configProvidersUseCase, coronaCheckRepository, getEventProvidersWithTokensUseCase, getRemoteEventsUseCase)
        return getEventsUseCase.getEvents(jwt, originType, targetProviderIds)
    }
    
    @Test
    fun `given config providers call returns error then getEvents returns EventsResultError`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Error(
            eventsError
        )

        val eventsResult = getEvents()

        assertTrue(eventsResult is EventsResult.Error)
    }

    @Test
    fun `given access tokens call returns error then getEvents returns EventsResultError`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(mockk())
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns mockk<NetworkRequestResult.Failed.Error>()

        val eventsResult = getEvents()

        assertTrue(eventsResult is EventsResult.Error)
    }

    @Test
    fun `given unomi call returns error then getEvents returns EventsResultError`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(mockk())
        val remoteAccessTokens = RemoteAccessTokens(listOf())
        val tokensResult = NetworkRequestResult.Success(remoteAccessTokens)
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns tokensResult
        val httpError = httpError()
        val eventProviderWithTokenResult = EventProviderWithTokenResult.Error(httpError)
        coEvery { getEventProvidersWithTokensUseCase.get(any(), any(), any(), any()) } returns listOf(eventProviderWithTokenResult, eventProviderWithTokenResult)

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.Error(listOf(httpError, httpError)),
            eventsResult
        )
    }

    @Test
    fun `given unomi call returns no events then getEvents returns EventsResultHasNoEvents`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(mockk())
        val remoteAccessTokens = RemoteAccessTokens(listOf())
        val tokensResult = NetworkRequestResult.Success(remoteAccessTokens)
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns tokensResult
        coEvery { getEventProvidersWithTokensUseCase.get(any(), any(), any(), any()) } returns listOf()

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.HasNoEvents(false),
            eventsResult
        )
    }

    @Test
    fun `given getRemoteEvents call returns two events then getEvents returns EventsResultSuccess`() = runBlocking {
        val (provider1, provider2) = mockProvidersResult()
        val signedModel1: SignedResponseWithModel<RemoteProtocol3> = mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
            coEvery { model.events } returns listOf(mockk())
        }
        val signedModel2: SignedResponseWithModel<RemoteProtocol3> = mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
            coEvery { model.events } returns listOf(mockk())
        }
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider1, any(), any()) } returns RemoteEventsResult.Success(signedModel1)
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider2, any(), any()) } returns RemoteEventsResult.Success(signedModel2)

        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(
            emptyList())

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.Success(listOf(signedModel1, signedModel2), false, emptyList()),
            eventsResult
        )
    }

    @Test
    fun `given getRemoteEvents call returns one event and one missing events then getEvents returns EventsResultSuccess`() = runBlocking {
        val (provider1, provider2) = mockProvidersResult()
        val signedModel1: SignedResponseWithModel<RemoteProtocol3> = mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
            coEvery { model.events } returns listOf(mockk())
        }
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider1, any(), any()) } returns RemoteEventsResult.Success(signedModel1)
        val httpError = httpError()
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider2, any(), any()) } returns RemoteEventsResult.Error(httpError)

        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(
            emptyList())

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.Success(listOf(signedModel1), true, emptyList()),
            eventsResult
        )
    }

    @Test
    fun `given getRemoteEvents call returns one missing event and one error then getEvents returns EventsResultSuccess`() = runBlocking {
        val (provider1, provider2) = mockProvidersResult()
        val signedModel1: SignedResponseWithModel<RemoteProtocol3> = mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
            coEvery { model.events } returns listOf()
        }

        val httpError = httpError()
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider1, any(), any()) } returns RemoteEventsResult.Success(signedModel1)
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider2, any(), any()) } returns RemoteEventsResult.Error(httpError)

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.HasNoEvents(true, listOf(httpError)),
            eventsResult
        )
    }

    @Test
    fun `given getRemoteEvents call returns two errors then getEvents returns EventsResultError`() = runBlocking {
        val (provider1, provider2) = mockProvidersResult()

        val httpError = httpError()
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider1, any(), any()) } returns RemoteEventsResult.Error(httpError)
        coEvery { getRemoteEventsUseCase.getRemoteEvents(provider2, any(), any()) } returns RemoteEventsResult.Error(httpError)

        val eventsResult = getEvents()

        assertEquals(
            EventsResult.Error(listOf(httpError, httpError)),
            eventsResult
        )
    }
    
    private fun httpError(): NetworkRequestResult.Failed {
        val httpException = HttpException(
            Response.error<String>(
                404, "".toResponseBody()
            )
        )
        return NetworkRequestResult.Failed.CoronaCheckHttpError(
            HolderStep.UnomiNetworkRequest, httpException
        )
    }

    private suspend fun mockProvidersResult(): Pair<RemoteConfigProviders.EventProvider, RemoteConfigProviders.EventProvider> {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(mockk())
        val token1 = mockk<RemoteAccessTokens.Token>()
        val token2 = mockk<RemoteAccessTokens.Token>()
        val remoteAccessTokens: RemoteAccessTokens = mockk<RemoteAccessTokens>().apply {
            coEvery { tokens } returns listOf(token1, token2)
        }
        val tokensResult = NetworkRequestResult.Success(remoteAccessTokens)
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns tokensResult
        val provider1 = mockk<RemoteConfigProviders.EventProvider>()
        val provider2 = mockk<RemoteConfigProviders.EventProvider>()

        coEvery { getEventProvidersWithTokensUseCase.get(any(), any(), any(), any()) } returns listOf(EventProviderWithTokenResult.Success(provider1, token1), EventProviderWithTokenResult.Success(provider2, token2))

        return Pair(provider1, provider2)
    }
}