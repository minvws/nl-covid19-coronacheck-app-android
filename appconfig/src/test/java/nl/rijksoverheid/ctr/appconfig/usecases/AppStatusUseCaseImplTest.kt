package nl.rijksoverheid.ctr.appconfig.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.fakeAppConfig
import nl.rijksoverheid.ctr.appconfig.fakeAppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppStatusUseCaseImplTest {

    private val publicKeys = "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType()).source()
    private fun getAppConfig(minimumVersion: Int = 1, appDeactivated: Boolean = false) = AppConfig(
        minimumVersion = minimumVersion,
        appDeactivated = appDeactivated,
        informationURL = "dummy",
        configTtlSeconds = 0,
        maxValidityHours = 0,
        euLaunchDate = "",
        credentialRenewalDays = 0,
        domesticCredentialValidity = 0,
        testEventValidity = 0,
        recoveryEventValidity = 0,
        temporarilyDisabled = false,
        requireUpdateBefore = 0
    )

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() =
        runBlocking {
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager()
            )

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
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager()
            )

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
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager()
            )

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
    fun `status returns InternetRequired when config is Error and cached app config does not exist`() =
        runBlocking {
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = null
                ),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager()
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.InternetRequired, appStatus)
        }

    @Test
    fun `status returns InternetRequired when config is Error and cached app config is no longer valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 20 seconds
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = fakeAppConfig(
                        configTtlSeconds = 50
                    )
                ),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(
                    lastFetchedSeconds = 20
                )
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.InternetRequired, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when config is Error and cached app config is still valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 70 seconds
            val appStatusUseCase = AppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = fakeAppConfig(
                        configTtlSeconds = 50
                    )
                ),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(
                    lastFetchedSeconds = 70
                )
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.NoActionRequired, appStatus)
        }
}
