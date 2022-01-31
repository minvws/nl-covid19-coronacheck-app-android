package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.exceptions.NoProvidersException
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetMijnCnEventsUseCaseImplTest {
    private val configProvidersUseCase: ConfigProvidersUseCase = mockk()
    private val coronaCheckRepository: CoronaCheckRepository = mockk()
    private val getEventProvidersWithTokensUseCase: GetEventProvidersWithTokensUseCase = mockk()
    private val getRemoteEventsUseCase: GetRemoteEventsUseCase = mockk()

    private val eventsError = mockk<NetworkRequestResult.Failed.Error>()
    private val jwt = "jwt"
    private val originType = RemoteOriginType.Test

    private val remoteEventProviders = listOf(eventProvider1, eventProvider2)
    val eventProviders = remoteEventProviders.map { EventProvider(it.providerIdentifier, it.name) }

    private suspend fun getEvents(): EventsResult {
        val getEventsUseCase = GetMijnCnEventsUsecaseImpl(
            configProvidersUseCase,
            getRemoteEventsUseCase
        )
        return getEventsUseCase.getEvents(jwt, originType, false)
    }

    @Test
    fun `given config providers call returns error then getEvents returns EventsResultError`() =
        runBlocking {
            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Error(
                eventsError
            )

            val eventsResult = getEvents()

            assertTrue(eventsResult is EventsResult.Error)
        }


    @Test
    fun `given getRemoteEvents call returns two events then getEvents returns EventsResultSuccess`() =
        runBlocking {
            val (provider1, provider2) = mockProvidersResult()
            val signedModel1: SignedResponseWithModel<RemoteProtocol3> =
                mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
                    coEvery { model.events } returns listOf(mockk())
                }
            val signedModel2: SignedResponseWithModel<RemoteProtocol3> =
                mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
                    coEvery { model.events } returns listOf(mockk())
                }
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider1,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Success(signedModel1)
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider2,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Success(signedModel2)

            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(provider1, provider2)
            )

            val eventsResult = getEvents()

            assertEquals(
                EventsResult.Success(listOf(signedModel1, signedModel2), false, eventProviders),
                eventsResult
            )
        }

    @Test
    fun `given getRemoteEvents call returns one event and one missing events then getEvents returns EventsResultSuccess`() =
        runBlocking {
            val (provider1, provider2) = mockProvidersResult()
            val signedModel1: SignedResponseWithModel<RemoteProtocol3> =
                mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
                    coEvery { model.events } returns listOf(mockk())
                }
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider1,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Success(signedModel1)
            val httpError = httpError()
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider2,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)

            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(eventProvider1, eventProvider2)
            )

            val eventsResult = getEvents()

            assertEquals(
                EventsResult.Success(listOf(signedModel1), true, eventProviders),
                eventsResult
            )
        }

    @Test
    fun `given getRemoteEvents call returns one missing event and one error then getEvents returns EventsResultHasNoEvents`() =
        runBlocking {
            val (provider1, provider2) = mockProvidersResult()
            val signedModel1: SignedResponseWithModel<RemoteProtocol3> =
                mockk<SignedResponseWithModel<RemoteProtocol3>>().apply {
                    coEvery { model.events } returns listOf()
                }

            val httpError = httpError()
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider1,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Success(signedModel1)
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider2,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)
            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(eventProvider1, eventProvider2)
            )

            val eventsResult = getEvents()

            assertEquals(
                EventsResult.HasNoEvents(true, listOf(httpError)),
                eventsResult
            )
        }

    @Test
    fun `given getRemoteEvents call returns two errors then getEvents returns EventsResultError`() =
        runBlocking {
            val (provider1, provider2) = mockProvidersResult()
            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(eventProvider1, eventProvider2)
            )

            val httpError = httpError()
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider1,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider2,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)

            val eventsResult = getEvents()

            assertEquals(
                EventsResult.Error(listOf(httpError, httpError)),
                eventsResult
            )
        }

    @Test
    fun `given configProviders with no matching provider identifiers, when getEvents, then returns no provider error`() =
        runBlocking {
            val eventProvider = eventProvider1.copy(usage = listOf(""))
            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(eventProvider)
            )
            val remoteAccessTokens = RemoteAccessTokens(listOf())
            val tokensResult = NetworkRequestResult.Success(remoteAccessTokens)
            coEvery { coronaCheckRepository.accessTokens("jwt") } returns tokensResult

            val eventsResult = getEvents()

            val exception = (eventsResult as EventsResult.Error).errorResults.first().getException()
            assertTrue(exception is NoProvidersException.Test)
        }


    @Test
    fun `given getRemoteEvents call returns MijnCnMissingData error then returns EventsResultError`() =
        runBlocking {
            val (provider1, provider2) = mockProvidersResult()
            val httpError = mijnCnMissingData()
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider1,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)
            coEvery {
                getRemoteEventsUseCase.getRemoteEvents(
                    provider2,
                    any(),
                    any(),
                    any()
                )
            } returns RemoteEventsResult.Error(httpError)

            coEvery { configProvidersUseCase.eventProvidersBES() } returns EventProvidersResult.Success(
                listOf(eventProvider1, eventProvider2)
            )

            val eventsResult = getEvents()

            assertEquals(
                EventsResult.Error(listOf(httpError, httpError)),
                eventsResult
            )
            assertEquals((eventsResult as EventsResult.Error).isMijnCnMissingDataErrors(), true)
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

    private fun mijnCnMissingData(): NetworkRequestResult.Failed {
        val httpException = HttpException(
            Response.error<String>(
                500, "".toResponseBody()
            )
        )
        return NetworkRequestResult.Failed.CoronaCheckWithErrorResponseHttpError(
            HolderStep.EventNetworkRequest, httpException, CoronaCheckErrorResponse("", 777706)
        )
    }

    private suspend fun mockProvidersResult(): Pair<RemoteConfigProviders.EventProvider, RemoteConfigProviders.EventProvider> {
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(
            remoteEventProviders
        )
        val token1 = mockk<RemoteAccessTokens.Token>()
        val token2 = mockk<RemoteAccessTokens.Token>()
        val remoteAccessTokens: RemoteAccessTokens = mockk<RemoteAccessTokens>().apply {
            coEvery { tokens } returns listOf(token1, token2)
        }
        val tokensResult = NetworkRequestResult.Success(remoteAccessTokens)
        coEvery { coronaCheckRepository.accessTokens("jwt") } returns tokensResult

        coEvery {
            getEventProvidersWithTokensUseCase.get(
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(
            EventProviderWithTokenResult.Success(eventProvider1, token1),
            EventProviderWithTokenResult.Success(eventProvider2, token2)
        )

        return Pair(eventProvider1, eventProvider2)
    }
}
