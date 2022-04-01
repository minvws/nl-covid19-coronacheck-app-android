package nl.rijksoverheid.ctr.verifier.persistance.usecase

import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase

interface VerifierCachedAppConfigUseCase {
    fun getCachedAppConfig(): VerifierConfig
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierCachedAppConfigUseCaseImpl constructor(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : VerifierCachedAppConfigUseCase {

    override fun getCachedAppConfig(): VerifierConfig {
        return cachedAppConfigUseCase.getCachedAppConfig() as VerifierConfig
    }
}
