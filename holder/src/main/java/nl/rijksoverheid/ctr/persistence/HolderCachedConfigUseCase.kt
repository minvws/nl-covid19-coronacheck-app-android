package nl.rijksoverheid.ctr.persistence

import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.shared.DebugDisclosurePolicyPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase as BaseCachedAppConfigUseCase

interface CachedAppConfigUseCase {
    fun getCachedAppConfig(): HolderConfig
    fun getCachedAppConfigOrNull(): HolderConfig?
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CachedAppConfigUseCaseImpl constructor(
    private val baseCachedAppConfigUseCase: BaseCachedAppConfigUseCase,
    private val isDebugApp: Boolean,
    private val debugDisclosurePolicyPersistenceManager: DebugDisclosurePolicyPersistenceManager
) : CachedAppConfigUseCase {

    private val defaultConfig = HolderConfig.default()

    override fun getCachedAppConfig(): HolderConfig {
        return getCachedAppConfigOrNull() ?: defaultConfig
    }

    override fun getCachedAppConfigOrNull(): HolderConfig? {
        val config = baseCachedAppConfigUseCase.getCachedAppConfigOrNull() as? HolderConfig

        val debugPolicy = debugDisclosurePolicyPersistenceManager.getDebugDisclosurePolicy()

        return if (isDebugApp && debugPolicy != null) {
            config?.copy(disclosurePolicy = debugPolicy)
        } else {
            config
        }
    }
}
