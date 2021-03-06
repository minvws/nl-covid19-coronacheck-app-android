package nl.rijksoverheid.ctr.appconfig.usecase

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface PersistConfigUseCase {
    suspend fun persist(appConfig: AppConfig, publicKeys: PublicKeys)
}

class PersistConfigUseCaseImpl(
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
) : PersistConfigUseCase {

    private val moshi = Moshi.Builder().build()

    override suspend fun persist(appConfig: AppConfig, publicKeys: PublicKeys) =
        withContext(Dispatchers.IO) {
            appConfigPersistenceManager.saveAppConfigJson(
                json = appConfig.toJson(moshi)
            )
            appConfigPersistenceManager.savePublicKeysJson(
                json = publicKeys.toJson(moshi)
            )
        }
}
