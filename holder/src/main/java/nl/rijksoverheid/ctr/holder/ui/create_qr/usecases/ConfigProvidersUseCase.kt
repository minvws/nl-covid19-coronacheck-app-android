package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import java.io.File

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
    fun getProviderName(providerIdentifier: String): String
}

class ConfigProvidersUseCaseImpl(
    private val coronaCheckRepository: CoronaCheckRepository,
    private val moshi: Moshi,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val filesDirPath: String,) :
    ConfigProvidersUseCase {

    private val eventProvidersFile = File(filesDirPath, "event_providers.json")

    override suspend fun testProviders(): TestProvidersResult {
        return when (val result = coronaCheckRepository.configProviders()) {
            is NetworkRequestResult.Success<RemoteConfigProviders> -> TestProvidersResult.Success(result.response.testProviders)
            is NetworkRequestResult.Failed -> TestProvidersResult.Error(result)
        }
    }

    override suspend fun eventProviders(): EventProvidersResult {
        return when (val result = coronaCheckRepository.configProviders()) {
            is NetworkRequestResult.Success<RemoteConfigProviders> -> {
                appConfigStorageManager.storageFile(eventProvidersFile, moshi.adapter(RemoteConfigProviders::class.java).toJson(result.response))
                EventProvidersResult.Success(result.response.eventProviders)
            }
            is NetworkRequestResult.Failed -> EventProvidersResult.Error(result)
        }
    }

    override fun getProviderName(providerIdentifier: String): String {
        val remoteConfigProviders: RemoteConfigProviders? = appConfigStorageManager.getFileAsBufferedSource(eventProvidersFile)?.readUtf8()?.toObject(moshi)

        return remoteConfigProviders?.eventProviders
            ?.firstOrNull { it.providerIdentifier == providerIdentifier }
            ?.name
            ?: providerIdentifier
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
