package nl.rijksoverheid.ctr.usecases

import com.squareup.moshi.Moshi
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.json.DisclosurePolicyJsonAdapter
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.fakeAppConfig
import nl.rijksoverheid.ctr.fakeAppConfigPersistenceManager
import nl.rijksoverheid.ctr.fakeCachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderAppStatusUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecases.ShowNewDisclosurePolicyUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderAppStatusUseCaseImplTest {

    private val moshi = Moshi
        .Builder()
        .add(DisclosurePolicyJsonAdapter())
        .build()

    private val publicKeys =
        "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType()).source().readUtf8()

    private fun getHolderConfig(
        minimumVersion: Int = 1,
        appDeactivated: Boolean = false,
        recommendedVersion: Int = 1
    ): String =
        HolderConfig.default(
            holderMinimumVersion = minimumVersion,
            holderAppDeactivated = appDeactivated,
            holderInformationURL = "dummy",
            holderRecommendedVersion = recommendedVersion
        ).toJson(moshi).toResponseBody("application/json".toMediaType()).source()
            .readUtf8()

    @Test
    fun `status returns Deactivated when app is deactivated remotely`() =
        runBlocking {
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getHolderConfig(appDeactivated = true),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            assertEquals(AppStatus.Deactivated, appStatus)
        }

    @Test
    fun `status returns UpdateRequired when remote version code is higher than current`() =
        runBlocking {
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getHolderConfig(minimumVersion = 2),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            assertEquals(AppStatus.UpdateRequired, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when app is up to date`() =
        runBlocking {
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(0), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = mockk(relaxed = true),
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk(relaxed = true),
                introductionPersistenceManager = mockk(relaxed = true),
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getHolderConfig(),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 1
            )
            assertEquals(AppStatus.NoActionRequired, appStatus)
        }

    @Test
    fun `status returns InternetRequired when config is Error and cached app config is no longer valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 20 seconds
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
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
                appUpdateData = mockk(),
                appUpdatePersistenceManager = mockk(),
                introductionPersistenceManager = mockk(),
                persistenceManager = mockk(),
                showNewDisclosurePolicyUseCase = mockk()
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1
            )
            assertEquals(AppStatus.Error, appStatus)
        }

    @Test
    fun `status returns NoActionRequired when config is Error and cached app config is still valid`() =
        runBlocking {

            // Current time is 100 seconds
            // Max offline time is set to 50 seconds
            // Last time config fetched was 70 seconds
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
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
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk(relaxed = true)
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Error,
                currentVersionCode = 1026
            )
            assertEquals(AppStatus.NoActionRequired, appStatus)
        }

    @Test
    fun `status is update recommended when holder version is higher and not shown before`() =
        runBlocking {
            val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager =
                mockk(relaxed = true) {
                    every { getHolderVersionUpdateShown() } returns 0
                }
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(10000), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = recommendedUpdatePersistenceManager,
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk {
                    every { getNewFeaturesSeen(any()) } returns true
                    every { getNewTermsSeen(any()) } returns true
                },
                introductionPersistenceManager = mockk {
                    every { getIntroductionFinished() } returns false
                },
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk {
                    every { get() } returns null
                }
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getHolderConfig(recommendedVersion = 2001),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 2000
            )

            coVerify { recommendedUpdatePersistenceManager.saveHolderVersionShown(2001) }
            assertEquals(AppStatus.UpdateRecommended, appStatus)
        }

    @Test
    fun `status is no action required recommended version was shown before`() =
        runBlocking {
            val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager =
                mockk(relaxed = true) {
                    every { getHolderVersionUpdateShown() } returns 2001
                }
            val appStatusUseCase = HolderAppStatusUseCaseImpl(
                clock = Clock.fixed(Instant.ofEpochSecond(10000), ZoneId.of("UTC")),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                appConfigPersistenceManager = fakeAppConfigPersistenceManager(),
                moshi = moshi,
                recommendedUpdatePersistenceManager = recommendedUpdatePersistenceManager,
                appUpdateData = getAppUpdateData(),
                appUpdatePersistenceManager = mockk {
                    every { getNewFeaturesSeen(any()) } returns true
                    every { getNewTermsSeen(any()) } returns true
                },
                introductionPersistenceManager = mockk {
                    every { getIntroductionFinished() } returns false
                },
                persistenceManager = mockk(relaxed = true),
                showNewDisclosurePolicyUseCase = mockk {
                    every { get() } returns null
                }
            )

            val appStatus = appStatusUseCase.get(
                config = ConfigResult.Success(
                    appConfig = getHolderConfig(recommendedVersion = 2001),
                    publicKeys = publicKeys
                ),
                currentVersionCode = 2000
            )

            assertEquals(AppStatus.NoActionRequired, appStatus)
        }

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
    fun `when new features are available, the status is new features`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        assertTrue(
            appStatus is AppStatus.NewFeatures
        )
    }

    @Test
    fun `when new terms are available, the status is consent needed`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { appUpdatePersistenceManager.getNewTermsSeen(1) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns null
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        assertTrue(
            appStatus is AppStatus.ConsentNeeded
        )
    }

    @Test
    fun `when disclosure is 1G+3G, there should be a 1G+3G new feature item added`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneAndThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneAndThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_3Gand1G_title,
                appUpdateData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_3Gand1G_body,
                appUpdateData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                appUpdateData.newFeatures.last().imageResource
            )
            assertEquals(
                R.string.new_in_app_subtitle,
                appUpdateData.newFeatures.last().subtitleResource
            )
            assertEquals(2, appUpdateData.newFeatures.size)
        }
    }

    @Test
    fun `when disclosure is 1G, there should be a 1G new feature item added`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )
        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only1G_title,
                appUpdateData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only1G_body,
                appUpdateData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                appUpdateData.newFeatures.last().imageResource
            )
            assertEquals(
                R.string.general_newpolicy,
                appUpdateData.newFeatures.last().subtitleResource
            )
            assertEquals(2, appUpdateData.newFeatures.size)
        }
    }

    @Test
    fun `when disclosure is 3G, there should be a 3G new feature item added`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.OneAndThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only3G_title,
                appUpdateData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only3G_body,
                appUpdateData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                appUpdateData.newFeatures.last().imageResource
            )
            assertEquals(
                R.string.general_newpolicy,
                appUpdateData.newFeatures.last().subtitleResource
            )
            assertEquals(2, appUpdateData.newFeatures.size)
        }
    }

    @Test
    fun `when disclosure is 0G, there should be a 0G new feature item added`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ZeroG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_onlyInternationalCertificates_0G_title,
                appUpdateData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_onlyInternationalCertificates_0G_body,
                appUpdateData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                appUpdateData.newFeatures.last().imageResource
            )
            assertEquals(
                R.string.new_in_app_subtitle,
                appUpdateData.newFeatures.last().subtitleResource
            )
            assertEquals(2, appUpdateData.newFeatures.size)
        }
    }

    @Test
    fun `when there are no new feature but there is a policy change, there should be a new feature item`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.OneAndThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only3G_title,
                appUpdateData.newFeatures.first().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only3G_body,
                appUpdateData.newFeatures.first().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                appUpdateData.newFeatures.first().imageResource
            )
            assertEquals(1, appUpdateData.newFeatures.size)
            assertEquals(2, appUpdateData.newFeatureVersion)
        }
    }

    @Test
    fun `when there is new feature but no policy change, there should not be a policy new feature item`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns null
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(1, appUpdateData.newFeatures.size)
            assertEquals(2, appUpdateData.newFeatureVersion)
        }
    }

    @Test
    fun `given config result error and a corrupted cached config, AppStatus is Error`() = runBlocking {
        val appStatusUseCase = appStatusUseCase(false, 1000, cachedAppConfig = null)

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Error,
            currentVersionCode = 1000
        )

        assertEquals(AppStatus.Error, appStatus)
    }

    @Test
    fun `given a valid cached config fetched recently, when config result is error, then no action is required`() = runBlocking {
        val appStatusUseCase = appStatusUseCase(
            appDeactivated = false,
            minimumVersion = 1000,
            configLastFetchedSeconds = 10000,
            configTtlSeconds = 1000,
            showNewDisclosurePolicyUseCase = mockk {
                every { get() } returns null
            },
            appUpdatePersistenceManager = mockk {
                every { getNewFeaturesSeen(any()) } returns true
                every { getNewTermsSeen(any()) } returns true
            }
        )

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Error,
            currentVersionCode = 1000
        )

        assertEquals(AppStatus.NoActionRequired, appStatus)
    }

    @Test
    fun `given a valid cached config fetched long time ago, when config result is error, then AppStatus is error`() = runBlocking {
        val appStatusUseCase = appStatusUseCase(
            appDeactivated = false,
            minimumVersion = 1000,
            configLastFetchedSeconds = 1000,
            configTtlSeconds = 1000
        )

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Error,
            currentVersionCode = 1000
        )

        assertEquals(AppStatus.Error, appStatus)
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

    @Test
    fun `when there is switch from 0G to CTB, there should be a new feature item`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ZeroG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_dutchAndInternationalCertificates_title,
                appUpdateData.newFeatures.first().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_dutchAndInternationalCertificates_body,
                appUpdateData.newFeatures.first().description
            )
            assertEquals(
                R.drawable.illustration_new_dutch_and_international_certificate,
                appUpdateData.newFeatures.first().imageResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_dutchAndInternationalCertificates_button_toMyCertificates,
                appUpdateData.newFeatures.first().buttonResource
            )
            assertEquals(
                R.string.new_in_app_subtitle,
                appUpdateData.newFeatures.first().subtitleResource
            )
            assertEquals(2, appUpdateData.newFeatures.size)
        }
    }

    @Test
    fun `when switching policy and there is no change in CTB, there should not be a new feature item`() = runBlocking {
        val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
        val appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk()
        val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
        val persistenceManager: PersistenceManager = mockk()
        val appStatusUseCase = appStatusUseCase(
            false, 1000, getAppUpdateData(), appUpdatePersistenceManager,
            introductionPersistenceManager, showNewDisclosurePolicyUseCase,
            persistenceManager = persistenceManager
        )

        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { appUpdatePersistenceManager.getNewFeaturesSeen(2) } returns true
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.OneG

        val appStatus = appStatusUseCase.get(
            config = ConfigResult.Success(
                appConfig = getHolderConfig(),
                publicKeys = publicKeys
            ),
            currentVersionCode = 1
        )

        with(appStatus as AppStatus.NewFeatures) {
            assertEquals(1, appUpdateData.newFeatures.size)
        }
    }

    private fun appStatusUseCase(
        appDeactivated: Boolean,
        minimumVersion: Int,
        appUpdateData: AppUpdateData = getAppUpdateData(),
        appUpdatePersistenceManager: AppUpdatePersistenceManager = mockk(),
        introductionPersistenceManager: IntroductionPersistenceManager = mockk(),
        showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk(),
        cachedAppConfig: HolderConfig? = mockk(),
        configLastFetchedSeconds: Long = 0,
        configTtlSeconds: Int = 0,
        clock: Clock = Clock.fixed(Instant.ofEpochSecond(10000), ZoneId.of("UTC")),
        persistenceManager: PersistenceManager = mockk()
    ) =
        HolderAppStatusUseCaseImpl(
            clock = clock,
            cachedAppConfigUseCase = mockk {
                every { getCachedAppConfigOrNull() } returns cachedAppConfig
                if (cachedAppConfig != null) {
                    every { getCachedAppConfig() } returns cachedAppConfig
                    every { cachedAppConfig.appDeactivated } returns appDeactivated
                    every { cachedAppConfig.minimumVersion } returns minimumVersion
                    every { cachedAppConfig.configTtlSeconds } returns configTtlSeconds
                    every { cachedAppConfig.recommendedVersion } returns 1000
                }
            },
            appConfigPersistenceManager = mockk<AppConfigPersistenceManager>().apply {
                every { getAppConfigLastFetchedSeconds() } returns configLastFetchedSeconds
            },
            moshi = moshi,
            recommendedUpdatePersistenceManager = mockk(relaxed = true),
            appUpdateData = appUpdateData,
            appUpdatePersistenceManager = appUpdatePersistenceManager,
            introductionPersistenceManager = introductionPersistenceManager,
            persistenceManager = persistenceManager,
            showNewDisclosurePolicyUseCase = showNewDisclosurePolicyUseCase
        )
}
