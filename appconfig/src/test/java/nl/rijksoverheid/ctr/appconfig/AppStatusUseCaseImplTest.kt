package nl.rijksoverheid.ctr.appconfig

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecase.AppStatusUseCaseImpl
import org.junit.Assert
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppStatusUseCaseImplTest {

    private val appStatusUseCase = AppStatusUseCaseImpl()
    private val publicKeys = PublicKeys(clKeys = listOf())
    private fun getAppConfig(minimumVersion: Int = 1, appDeactivated: Boolean = false) = AppConfig(
        minimumVersion = minimumVersion,
        appDeactivated = appDeactivated,
        informationURL = "dummy",
        configTtlSeconds = 0,
        maxValidityHours = 0
    )

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() =
        runBlocking {
            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getAppConfig(appDeactivated = true),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.Deactivated("dummy"), appStatus)
        }

    @Test
    fun `status returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getAppConfig(minimumVersion = 2),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.UpdateRequired, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when app is up to date`() =
        runBlocking {
            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getAppConfig(),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.NoActionRequired, appStatus)
        }

    @Test
    fun `status returns InternetRequired when config is Network Error`() =
        runBlocking {
            val appStatus = appStatusUseCase.get(
                config = ConfigResult.NetworkError,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.InternetRequired, appStatus)
        }

    @Test
    fun `status returns InternetRequired when config is Server Error`() =
        runBlocking {
            val appStatus = appStatusUseCase.get(
                config = ConfigResult.ServerError,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.InternetRequired, appStatus)
        }
}
