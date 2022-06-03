package nl.rijksoverheid.ctr.appconfig.usecases

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ConfigResultUseCase {
    suspend fun fetch(): ConfigResult
}

class ConfigResultUseCaseImpl(
    private val appConfigUseCase: AppConfigUseCase,
    private val persistConfigUseCase: PersistConfigUseCase,
): ConfigResultUseCase {

    private val mutex = Mutex()

    override suspend fun fetch(): ConfigResult {
        // allow only one config/public keys refresh at a time
        // cause we store them writing to files and a parallel
        // operation could break them eventually
        mutex.withLock {
            val configResult = appConfigUseCase.get()
            if (configResult is ConfigResult.Success) {
                persistConfigUseCase.persist(
                    appConfigContents = configResult.appConfig,
                    publicKeyContents = configResult.publicKeys
                )
            }
            return configResult
        }
    }
}