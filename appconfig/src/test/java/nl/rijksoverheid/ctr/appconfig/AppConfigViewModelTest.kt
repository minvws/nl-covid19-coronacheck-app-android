package nl.rijksoverheid.ctr.appconfig

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.LoadPublicKeysUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.PersistConfigUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import okio.BufferedSource
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppConfigViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val appConfigUseCase: AppConfigUseCase = mockk(relaxed = true)
    private val appStatusUseCase: AppStatusUseCase = mockk(relaxed = true)
    private val persistConfigUseCase: PersistConfigUseCase = mockk(relaxed = true)
    private val loadPublicKeyUseCase: LoadPublicKeysUseCase = mockk(relaxed = true)
    private val appConfigStorageManager: AppConfigStorageManager = mockk(relaxed = true)
    private val cachedAppConfigUseCase: CachedAppConfigUseCase = mockk(relaxed = true)
    private val appConfigViewModel = AppConfigViewModelImpl(
        appConfigUseCase = appConfigUseCase,
        appStatusUseCase = appStatusUseCase,
        persistConfigUseCase = persistConfigUseCase,
        loadPublicKeysUseCase = loadPublicKeyUseCase,
        appConfigStorageManager = appConfigStorageManager,
        cachedAppConfigUseCase = cachedAppConfigUseCase,
        cacheDirPath = "",
        isVerifierApp = false,
        versionCode = 0
    )
    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `refresh calls correct usecases when success`() = runBlocking {
        val appConfig = AppConfig(
            minimumVersion = 0,
            appDeactivated = false,
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
        val appConfigContents = "app config contents"

        val publicKeys = mockk<BufferedSource>()
        val publicKeysContents = "file contents"
        coEvery { publicKeys.readUtf8() } returns publicKeysContents

        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Success(
                appConfig = appConfigContents,
                publicKeys = publicKeysContents
            )
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }
        coEvery { cachedAppConfigUseCase.getCachedPublicKeys() } returns publicKeys

        appConfigViewModel.refresh(mobileCoreWrapper)

        coVerify { persistConfigUseCase.persist(appConfigContents, publicKeysContents) }
        coVerify { loadPublicKeyUseCase.load(publicKeys) }
    }

    @Test
    fun `refresh calls emits status to livedata`() = runBlocking {
        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Error
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.InternetRequired }

        appConfigViewModel.refresh(mobileCoreWrapper)

        Assert.assertEquals(appConfigViewModel.appStatusLiveData.value, AppStatus.InternetRequired)
    }

    @Test
    fun `refresh with no config files in verifier app emits internet required status`() = runBlocking {
        val appConfigViewModel = AppConfigViewModelImpl(
            appConfigUseCase = appConfigUseCase,
            appStatusUseCase = appStatusUseCase,
            persistConfigUseCase = persistConfigUseCase,
            loadPublicKeysUseCase = loadPublicKeyUseCase,
            appConfigStorageManager = appConfigStorageManager,
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            cacheDirPath = "",
            isVerifierApp = true,
            versionCode = 0
        )
        val appConfig = AppConfig(
            minimumVersion = 0,
            appDeactivated = false,
            informationURL = "dummy",
            configTtlSeconds = 0,
            maxValidityHours = 0
        )
        val appConfigContents = "app config contents"

        val publicKeys = mockk<BufferedSource>()
        val publicKeysContents = "file contents"
        coEvery { publicKeys.readUtf8() } returns publicKeysContents

        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Success(
                appConfig = appConfigContents,
                publicKeys = publicKeysContents
            )
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }
        coEvery { appConfigStorageManager.areConfigFilesPresent() } returns false

        appConfigViewModel.refresh(mobileCoreWrapper)

        Assert.assertEquals(appConfigViewModel.appStatusLiveData.value, AppStatus.InternetRequired)
    }

    @Test
    fun `refresh in the holder app has no interaction with config files and initialise the verifier`() {
        val appConfig = AppConfig(
            minimumVersion = 0,
            appDeactivated = false,
            informationURL = "dummy",
            configTtlSeconds = 0,
            maxValidityHours = 0
        )
        val appConfigContents = "app config contents"

        val publicKeys = mockk<BufferedSource>()
        val publicKeysContents = "file contents"
        coEvery { publicKeys.readUtf8() } returns publicKeysContents

        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Success(
                appConfig = appConfigContents,
                publicKeys = publicKeysContents
            )
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }

        appConfigViewModel.refresh(mobileCoreWrapper)

        coVerify(exactly = 0) { mobileCoreWrapper.initializeVerifier(any()) }
        coVerify(exactly = 0) { appConfigStorageManager.areConfigFilesPresent() }
    }
}
