package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.json.DisclosurePolicyJsonAdapter
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.models.*
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.verifier.fakeAppConfig
import nl.rijksoverheid.ctr.verifier.fakeAppConfigPersistenceManager
import nl.rijksoverheid.ctr.verifier.fakeCachedAppConfigUseCase
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Assert.*
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
class VerifierAppStatusUseCaseImplTest {

    private val moshi = Moshi
        .Builder()
        .add(DisclosurePolicyJsonAdapter())
        .build()

    private val publicKeys =
        "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType()).source().readUtf8()

    private fun getVerifierConfig(
        recommendedVersion: Int = 1,
        recommendedInterval: Int = 1,
        appDeactivated: Boolean = false,
        minimumVersion: Int = 1000,
    ): String =
        VerifierConfig.default(
            verifierRecommendedVersion = recommendedVersion,
            upgradeRecommendationIntervalHours = recommendedInterval,
            verifierAppDeactivated = appDeactivated,
            verifierMinimumVersion = minimumVersion,
        ).toJson(moshi).toResponseBody("application/json".toMediaType()).source()
            .readUtf8()

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() =
        runBlocking {
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getVerifierConfig(appDeactivated = true, minimumVersion = 1),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 2
            )
            Assert.assertEquals(AppStatus.Deactivated, appStatus)
        }

    @Test
    fun `status returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getVerifierConfig(minimumVersion = 2),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.UpdateRequired, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when app is up to date`() =
        runBlocking {
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getVerifierConfig(minimumVersion = 1),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.NoActionRequired, appStatus)
        }

    @Test
    fun `status returns InternetRequired when config is Error and cached app config is no longer valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 20 seconds
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = fakeAppConfig(
                        configTtlSeconds = 50
                    )
                ),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(
                    lastFetchedSeconds = 20
                ),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1
            )
            Assert.assertEquals(AppStatus.Error, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when config is Error and cached app config is still valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 70 seconds
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = fakeAppConfig(
                        configTtlSeconds = 50
                    )
                ),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(
                    lastFetchedSeconds = 70
                ),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1026
            )
            Assert.assertEquals(AppStatus.NoActionRequired, appStatus)
        }

    @Test
    fun `status is recommended update when verifier version is higher and it's shown after upgrade interval`() =
        runBlocking {
            val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager =
                mockk(relaxed = true) {
                    every { getRecommendedUpdateShownSeconds() } returns 0
                }
            val clock = Clock.fixed(Instant.ofEpochSecond(10000), ZoneId.of("UTC"))
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = clock,
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = recommendedUpdatePersistenceManager,
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getVerifierConfig(recommendedVersion = 2001),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 2000
            )

            coVerify { recommendedUpdatePersistenceManager.saveRecommendedUpdateShownSeconds(10000) }
            Assert.assertEquals(AppStatus.UpdateRecommended, appStatus)
        }

    @Test
    fun `status is no action required when verifier recommend update was shown in upgrade interval`() {
        runBlocking {
            val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager =
                mockk(relaxed = true) {
                    every { getRecommendedUpdateShownSeconds() } returns 3601
                }
            val appStatusUseCase = VerifierAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                featureFlagUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getVerifierConfig(
                        recommendedVersion = 2001,
                        recommendedInterval = 1
                    ),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 2000
            )

            Assert.assertEquals(AppStatus.NoActionRequired, appStatus)
        }
    }

    private fun appStatusUseCase(
        appDeactivated: Boolean,
        minimumVersion: Int,
        appUpdateData: AppUpdateData = getAppUpdateData(),
        appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk(),
        introductionPersistenceManager: IntroductionPersistenceManager = mockk(),
        featureFlagUseCase: FeatureFlagUseCase = mockk()
    ) =
        VerifierAppStatusUseCaseImpl(
            clock = Clock.fixed(Instant.ofEpochSecond(10000), ZoneId.of("UTC")),
            cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>().apply {
                every { getCachedAppConfig().appDeactivated } returns appDeactivated
                every { getCachedAppConfig().minimumVersion } returns minimumVersion
            },
            appConfigPersistenceManager = mockk(),
            moshi = moshi,
            recommendedUpdatePersistenceManager = mockk(relaxed = true),
            appUpdateData = appUpdateData,
            appUpdatePersistenceManager = appUpdatePersistenceManager,
            introductionPersistenceManager = introductionPersistenceManager,
            featureFlagUseCase = featureFlagUseCase
        )

    @Test
    fun `isAppActive returns false if is app is deactivated from the config`() {
        val appStatusUseCase = appStatusUseCase(true, 1000)

        assertFalse(appStatusUseCase.isAppActive(1000))
    }

    @Test
    fun `isAppActive returns false if app has forced update`() {
        val appStatusUseCase = appStatusUseCase(false, 1001)

        assertFalse(appStatusUseCase.isAppActive(1000))
    }

    @Test
    fun `isAppActive returns true if app is not deactivated and no forced update needed`() {
        val appStatusUseCase = appStatusUseCase(false, 1000)

        assertTrue(appStatusUseCase.isAppActive(1000))
    }

    @Test
    fun `given app is deactivated and needs a forced update, when config refreshes, then shows forced update status`() = runBlocking {
        val currentVersionCode = 1000
        val verifierMinimumVersion = 1001
        val configResult = ConfigResult.Success(
            getVerifierConfig(appDeactivated = true, minimumVersion = verifierMinimumVersion),
            publicKeys,
        )

        val appStatus = appStatusUseCase(true, verifierMinimumVersion).get(configResult, currentVersionCode)

        assertTrue(appStatus is AppStatus.UpdateRequired)
    }

    @Test
    fun `given app is deactivated and doesn't need a forced update, when config refreshes, then shows app deactivated status`() = runBlocking {
        val currentVersionCode = 1000
        val verifierMinimumVersion = 1000
        val configResult = ConfigResult.Success(
            getVerifierConfig(appDeactivated = true, minimumVersion = verifierMinimumVersion),
            publicKeys,
        )

        val appStatus = appStatusUseCase(true, verifierMinimumVersion).get(configResult, currentVersionCode)

        assertTrue(appStatus is AppStatus.Deactivated)
    }

    @Test
    fun `when new features are available, the status is new features`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        val appUpdateData = getAppUpdateData()
        val configResult = ConfigResult.Success(
            getVerifierConfig(appDeactivated = false, minimumVersion = 1),
            publicKeys,
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true

        val appStatusUseCase = appStatusUseCase(
            false, 1, appUpdateData, appUpdatePersistenceManager, introductionPersistenceManager, featureFlagUseCase
        )

        assertEquals(
            appStatusUseCase.get(configResult, 1),
            AppStatus.NewFeatures(appUpdateData)
        )
    }

    @Test
    fun `when new terms are available, the status is consent needed`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        val appUpdateData = getAppUpdateData()
        val configResult = ConfigResult.Success(
            getVerifierConfig(appDeactivated = false, minimumVersion = 1),
            publicKeys,
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { appUpdatePersistenceManager.getNewTermsSeen(1) } returns false
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true

        val appStatusUseCase = appStatusUseCase(
            false, 1, appUpdateData, appUpdatePersistenceManager, introductionPersistenceManager, featureFlagUseCase
        )

        assertEquals(
            appStatusUseCase.get(configResult, 1),
            AppStatus.ConsentNeeded(appUpdateData)
        )
    }

    @Test
    fun `when intro is finished and there are no new features or terms, the status is no action required`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val featureFlagUseCase: FeatureFlagUseCase = mockk()
        val appUpdateData = getAppUpdateData()
        val configResult = ConfigResult.Success(
            getVerifierConfig(appDeactivated = false, minimumVersion = 1),
            publicKeys,
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { appUpdatePersistenceManager.getNewTermsSeen(1) } returns true
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true

        val appStatusUseCase = appStatusUseCase(
            false, 1, appUpdateData, appUpdatePersistenceManager, introductionPersistenceManager, featureFlagUseCase
        )

        assertEquals(
            appStatusUseCase.get(configResult, 1),
            AppStatus.NoActionRequired
        )
    }

    private fun getAppUpdateData() = AppUpdateData(
        newTerms = NewTerms(
            version = 1,
            needsConsent = false
        ),
        newFeatures = listOf(
            NewFeatureItem(1, 2, 3)
        ),
        newFeatureVersion = 2,
        hideConsent = true
    )
}
