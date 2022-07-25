/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.get_events.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.fakeEventProviderRepository
import nl.rijksoverheid.ctr.holder.get_events.usecases.EventProviderWithTokenResult
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventProvidersWithTokensUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteUnomi
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.test.assertEquals

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

val eventProvider1 = RemoteConfigProviders.EventProvider(
    name = "event_provider_1",
    providerIdentifier = "event_provider_1",
    unomiUrl = "event_provider_1_url",
    eventUrl = "eventUrl",
    cms = listOf("".toByteArray()),
    tls = listOf("".toByteArray()),
    usage = listOf("pt",
        "nt",
        "r",
        "v"),
    auth = listOf("max")
)

val eventProvider2 = RemoteConfigProviders.EventProvider(
    name = "event_provider_2",
    providerIdentifier = "event_provider_2",
    unomiUrl = "event_provider_2_url",
    eventUrl = "eventUrl",
    cms = listOf("".toByteArray()),
    tls = listOf("".toByteArray()),
    usage = listOf("pt",
        "nt",
        "r",
        "v"),
    auth = listOf("max")
)

class GetEventProvidersWithTokensUseCaseImplTest {

    private val tokenProvider1 = RemoteAccessTokens.Token(
        providerIdentifier = "event_provider_1",
        unomi = "event_provider_1_unomi_token",
        event = "event_provider_1_event_token"
    )

    private val tokenProvider2 = RemoteAccessTokens.Token(
        providerIdentifier = "event_provider_2",
        unomi = "event_provider_2_unomi_token",
        event = "event_provider_2_event_token"
    )

    @Test
    fun `get() returns Success with event provider that return informationAvailable = true`() = runBlocking {
        val eventProviderRepository = fakeEventProviderRepository(
            unomi = {
                 when (it) {
                    "event_provider_1_url" -> {
                        NetworkRequestResult.Success(
                            RemoteUnomi(
                                providerIdentifier = "",
                                protocolVersion = "",
                                informationAvailable = true
                            )
                        )
                    }
                     else -> {
                         NetworkRequestResult.Success(
                             RemoteUnomi(
                                 providerIdentifier = "",
                                 protocolVersion = "",
                                 informationAvailable = false
                             )
                         )
                     }
                }
            }
        )

        val usecase = GetEventProvidersWithTokensUseCaseImpl(
            eventProviderRepository = eventProviderRepository
        )

        assertEquals(
            listOf(
                EventProviderWithTokenResult.Success(
                    eventProvider = eventProvider1,
                    token = tokenProvider1
            )),
            usecase.get(
                eventProviders = listOf(eventProvider1, eventProvider2),
                tokens = listOf(tokenProvider1, tokenProvider2),
                scope = null,
                filter = "vaccination"
            )
        )
    }

    @Test
    fun `get() returns ServerError for event provider that returns 404`() = runBlocking {
        val httpException = HttpException(
            Response.error<String>(
                404, "".toResponseBody()
            )
        )
        val httpError = NetworkRequestResult.Failed.CoronaCheckHttpError(
            HolderStep.UnomiNetworkRequest, httpException, null
        )
        val eventProviderRepository = fakeEventProviderRepository(
            unomi = {
                when (it) {
                    "event_provider_1_url" -> {
                        NetworkRequestResult.Success(
                            RemoteUnomi(
                                providerIdentifier = "",
                                protocolVersion = "",
                                informationAvailable = true
                            )
                        )
                    }
                    else -> {
                        httpError
                    }
                }
            }
        )

        val usecase = GetEventProvidersWithTokensUseCaseImpl(
            eventProviderRepository = eventProviderRepository
        )

        assertEquals(
            listOf(
                EventProviderWithTokenResult.Success(
                    eventProvider = eventProvider1,
                    token = tokenProvider1
                ),
                EventProviderWithTokenResult.Error(httpError)),
            usecase.get(
                eventProviders = listOf(eventProvider1, eventProvider2),
                tokens = listOf(tokenProvider1, tokenProvider2),
                filter = "vaccination",
                scope = null
            )
        )
    }

    @Test
    fun `get() returns Network for event provider that throws SocketTimeOutException`() = runBlocking {
        val exception = SocketTimeoutException()
        val eventProviderRepository = fakeEventProviderRepository(
            unomi = {
                when (it) {
                    "event_provider_1_url" -> {
                        NetworkRequestResult.Success(
                            RemoteUnomi(
                                providerIdentifier = "",
                                protocolVersion = "",
                                informationAvailable = true
                            )
                        )
                    }
                    else -> {
                        NetworkRequestResult.Failed.ServerNetworkError(
                            HolderStep.UnomiNetworkRequest, exception
                        )
                    }
                }
            }
        )

        val usecase = GetEventProvidersWithTokensUseCaseImpl(
            eventProviderRepository = eventProviderRepository
        )

        assertEquals(
            listOf(
                EventProviderWithTokenResult.Success(
                    eventProvider = eventProvider1,
                    token = tokenProvider1
                ),
                EventProviderWithTokenResult.Error(NetworkRequestResult.Failed.ServerNetworkError(
                    HolderStep.UnomiNetworkRequest, exception))),
            usecase.get(
                eventProviders = listOf(eventProvider1, eventProvider2),
                tokens = listOf(tokenProvider1, tokenProvider2),
                filter = "vaccination",
                scope = null
            )
        )
    }
}