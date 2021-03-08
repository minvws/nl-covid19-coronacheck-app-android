package nl.rijksoverheid.ctr.appconfig

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.shared.ext.toObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CachedAppConfigUseCase {
    fun persistAppConfig(appConfig: AppConfig)
    fun getCachedAppConfig(): AppConfig
    fun persistPublicKeys(publicKeys: PublicKeys)
    fun getCachedPublicKeys(): PublicKeys?
}

class CachedAppConfigUseCaseImpl constructor(
    private val persistenceManager: AppConfigPersistenceManager,
    private val moshi: Moshi
) : CachedAppConfigUseCase {

    override fun persistAppConfig(appConfig: AppConfig) {
        val json = appConfig.toJson(moshi)
        persistenceManager.saveAppConfigJson(json)
    }

    override fun getCachedAppConfig(): AppConfig {
        return persistenceManager.getAppConfigJson()?.toObject(moshi)
            ?: throw IllegalStateException("App config should be cached")
    }

    override fun persistPublicKeys(publicKeys: PublicKeys) {
        val json = publicKeys.toJson(moshi)
        persistenceManager.savePublicKeysJson(json)
    }

    override fun getCachedPublicKeys(): PublicKeys? {
        return persistenceManager.getPublicKeysJson()?.toObject(moshi)
    }
}
