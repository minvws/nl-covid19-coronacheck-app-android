package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventUseCase {
    suspend fun getEvents(): EventResult
}

class EventUseCaseImpl(
    private val configProvidersUseCase: ConfigProvidersUseCase,
    private val coronaCheckRepository: CoronaCheckRepository
) : EventUseCase {

    override suspend fun getEvents(): EventResult {
        return try {
            val eventProviders = configProvidersUseCase.eventProviders()
            Timber.v("VACFLOW: Fetched event providers: $eventProviders")

            val accessTokens = coronaCheckRepository.accessTokens("999999011")
            Timber.v("VACFLOW: Fetched access tokens: $accessTokens")

            EventResult.Success(
                remoteEvents = RemoteEvents("1")
            )
        } catch (ex: HttpException) {
            return EventResult.ServerError(ex.code())
        } catch (ex: IOException) {
            return EventResult.NetworkError
        }
    }
}

sealed class EventResult {
    data class Success(val remoteEvents: RemoteEvents) : EventResult()
    data class ServerError(val httpCode: Int) : EventResult()
    object NetworkError : EventResult()
}
