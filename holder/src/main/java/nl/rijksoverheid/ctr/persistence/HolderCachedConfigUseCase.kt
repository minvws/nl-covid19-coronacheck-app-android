package nl.rijksoverheid.ctr.persistence

import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase

interface HolderCachedAppConfigUseCase {
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
class HolderCachedAppConfigUseCaseImpl constructor(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : HolderCachedAppConfigUseCase {

    private val defaultConfig = HolderConfig.default()

    override fun getCachedAppConfig(): HolderConfig {
        return getCachedAppConfigOrNull() ?: defaultConfig
    }

    override fun getCachedAppConfigOrNull(): HolderConfig? {
        return cachedAppConfigUseCase.getCachedAppConfigOrNull() as? HolderConfig
    }
}
