package nl.rijksoverheid.ctr.appconfig.util

import android.content.Context
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.AppConfigUtilImpl
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppConfigUtilImplTest {

    @Test
    fun `getStringWithTestValidity returns string with configurable integer`() {
        val textResource = 0
        val maxValidityHours = 48
        val mockedContext: Context = mockk(relaxed = true)
        val fakeCachedAppConfigUseCase = object : CachedAppConfigUseCase {
            override fun persistAppConfig(appConfig: AppConfig) {

            }

            override fun getCachedAppConfig(): AppConfig = AppConfig(
                minimumVersion = 0,
                appDeactivated = false,
                informationURL = "dummy",
                configTtlSeconds = 0,
                maxValidityHours = maxValidityHours
            )

            override fun persistPublicKeys(publicKeys: PublicKeys) {

            }

            override fun getCachedPublicKeys(): PublicKeys? {
                return null
            }
        }

        val appConfigUtil = AppConfigUtilImpl(
            context = mockedContext,
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase
        )

        appConfigUtil.getStringWithTestValidity(textResource)
        verify { mockedContext.getString(textResource, maxValidityHours.toStr()) }
    }

}
