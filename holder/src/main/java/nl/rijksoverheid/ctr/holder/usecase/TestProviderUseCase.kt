package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestProviderUseCase(private val holderRepository: HolderRepository) {

    suspend fun testProvider(id: String): RemoteTestProviders.Provider? {
        return holderRepository.testProviders().providers.firstOrNull { it.providerIdentifier == id }
    }
}
