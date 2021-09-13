package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ConfigProvidersUseCase {
    suspend fun eventProviders(): EventProvidersResult
    suspend fun testProviders(): TestProvidersResult
}

class ConfigProvidersUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository) :
    ConfigProvidersUseCase {

    override suspend fun testProviders(): TestProvidersResult {
        return when (val result = coronaCheckRepository.configProviders()) {
            is NetworkRequestResult.Success<RemoteConfigProviders> -> TestProvidersResult.Success(result.response.testProviders)
            is NetworkRequestResult.Failed -> TestProvidersResult.Error(result)
        }
    }

    override suspend fun eventProviders(): EventProvidersResult {
        return when (val result = coronaCheckRepository.configProviders()) {
            is NetworkRequestResult.Success<RemoteConfigProviders> -> EventProvidersResult.Success(result.response.eventProviders)
            is NetworkRequestResult.Failed -> EventProvidersResult.Error(result)
        }
    }
}

sealed class TestProvidersResult {
    class Success(val testProviders: List<RemoteConfigProviders.TestProvider>): TestProvidersResult()
    class Error(val errorResult: ErrorResult): TestProvidersResult()
}

sealed class EventProvidersResult {
    class Success(val eventProviders: List<RemoteConfigProviders.EventProvider>): EventProvidersResult()
    class Error(val errorResult: ErrorResult): EventProvidersResult()
}
