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
    private val appConfigViewModel = AppConfigViewModelImpl(
        appConfigUseCase = appConfigUseCase,
        appStatusUseCase = appStatusUseCase,
        persistConfigUseCase = persistConfigUseCase,
        loadPublicKeysUseCase = loadPublicKeyUseCase,
        appConfigStorageManager = appConfigStorageManager,
        cacheDirPath = "",
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

        val publicKeys = PublicKeys(
            clKeys = listOf()
        )

        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Success(
                appConfig = appConfig,
                publicKeys = publicKeys
            )
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }

        appConfigViewModel.refresh(mobileCoreWrapper)

        coVerify { persistConfigUseCase.persist(appConfig, publicKeys) }
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
    fun `refresh with no config files emits internet required status`() = runBlocking {
        val appConfig = AppConfig(
            minimumVersion = 0,
            appDeactivated = false,
            informationURL = "dummy",
            configTtlSeconds = 0,
            maxValidityHours = 0
        )

        val publicKeys = PublicKeys(
            clKeys = listOf()
        )

        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Success(
                appConfig = appConfig,
                publicKeys = publicKeys
            )
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }
        coEvery { appConfigStorageManager.areConfigFilesPresent() } returns false

        appConfigViewModel.refresh(mobileCoreWrapper)

        Assert.assertEquals(appConfigViewModel.appStatusLiveData.value, AppStatus.InternetRequired)
    }
}
