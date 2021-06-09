package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ConfigProvidersUseCase {
    suspend fun eventProviders(): List<RemoteConfigProviders.EventProvider>
    suspend fun testProvider(id: String): RemoteConfigProviders.TestProvider?
}

class ConfigProvidersUseCaseImpl(private val coronaCheckRepository: CoronaCheckRepository) :
    ConfigProvidersUseCase {
    override suspend fun testProvider(id: String): RemoteConfigProviders.TestProvider? {
        return coronaCheckRepository.configProviders().testProviders.firstOrNull { it.providerIdentifier == id }
    }

    override suspend fun eventProviders(): List<RemoteConfigProviders.EventProvider> {
        return coronaCheckRepository.configProviders().eventProviders
    }
}
