package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.junit.Assert.assertTrue
import org.junit.Test

class GetEventsUseCaseImplTest {
    private val configProvidersUseCase: ConfigProvidersUseCase =  mockk()
    private val coronaCheckRepository: CoronaCheckRepository =  mockk()
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase =  mockk()
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase =  mockk()

    private val eventsError = mockk<NetworkRequestResult.Failed.Error<*>>()
    private val jwt = "jwt"
    private val originType = OriginType.Test
    private val targetProviderIds = listOf(RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_GGD)

    @Test
    fun `given config providers call returns error then getEvents returns EventsResultError`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Error(
            eventsError
        )

        val getEventsUseCase = GetEventsUseCaseImpl(configProvidersUseCase, coronaCheckRepository, getEventProvidersWithTokensUseCase, getRemoteEventsUseCase)
        val eventsResult = getEventsUseCase.getEvents(jwt, originType, targetProviderIds)

        assertTrue(eventsResult is EventsResult.Error)
    }

    @Test
    fun `given access tokens call returns error then getEvennts returns EventsResultError`() = runBlocking {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(mockk())
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns mockk<NetworkRequestResult.Failed.Error<RemoteAccessTokens>>()

        val getEventsUseCase = GetEventsUseCaseImpl(configProvidersUseCase, coronaCheckRepository, getEventProvidersWithTokensUseCase, getRemoteEventsUseCase)
        val eventsResult = getEventsUseCase.getEvents(jwt, originType, targetProviderIds)

        assertTrue(eventsResult is EventsResult.Error)
    }
}