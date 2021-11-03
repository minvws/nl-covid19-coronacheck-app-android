package nl.rijksoverheid.ctr.appconfig

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
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

    private val filesDirPath = "/files"

    private val appConfigUseCase = mockk<AppConfigUseCase>(relaxed = true).apply {
        every { canRefresh(any()) } returns true
    }
    private val appStatusUseCase: AppStatusUseCase = mockk(relaxed = true)
    private val persistConfigUseCase: PersistConfigUseCase = mockk(relaxed = true)
    private val appConfigStorageManager: AppConfigStorageManager = mockk(relaxed = true)
    private val cachedAppConfigUseCase: CachedAppConfigUseCase = mockk(relaxed = true)

    private fun appConfigViewModel(isVerifier: Boolean = false) = AppConfigViewModelImpl(
        appConfigUseCase = appConfigUseCase,
        appStatusUseCase = appStatusUseCase,
        persistConfigUseCase = persistConfigUseCase,
        appConfigStorageManager = appConfigStorageManager,
        cachedAppConfigUseCase = cachedAppConfigUseCase,
        filesDirPath = filesDirPath,
        isVerifierApp = isVerifier,
        versionCode = 0
    )
    private val mobileCoreWrapper: MobileCoreWrapper = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `given a happy flow on holder, then calls correct usecases only`() = runBlocking {
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
        coEvery { appConfigStorageManager.areConfigFilesPresentInFilesFolder() } returns true
        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }
        coEvery { mobileCoreWrapper.initializeHolder(filesDirPath) } returns null
        coEvery { cachedAppConfigUseCase.isCachedAppConfigValid() } returns true

        appConfigViewModel().refresh(mobileCoreWrapper)

        coVerify { persistConfigUseCase.persist(appConfigContents, publicKeysContents) }
        coVerify { mobileCoreWrapper.initializeHolder(filesDirPath) }
        coVerify(exactly = 0) { mobileCoreWrapper.initializeVerifier(filesDirPath) }
    }

    @Test
    fun `given a happy flow on verifier, then calls correct usecases only`() = runBlocking {
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
        coEvery { appConfigStorageManager.areConfigFilesPresentInFilesFolder() } returns true
        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.NoActionRequired }
        coEvery { mobileCoreWrapper.initializeVerifier(filesDirPath) } returns null
        coEvery { cachedAppConfigUseCase.isCachedAppConfigValid() } returns true

        appConfigViewModel(true).refresh(mobileCoreWrapper)

        coVerify { persistConfigUseCase.persist(appConfigContents, publicKeysContents) }
        coVerify(exactly = 0) { mobileCoreWrapper.initializeHolder(any()) }
        coVerify { mobileCoreWrapper.initializeVerifier(filesDirPath) }
    }

    @Test
    fun `refresh calls emits status to livedata`() = runBlocking {
        coEvery { appConfigUseCase.get() } answers {
            ConfigResult.Error
        }

        coEvery { appStatusUseCase.get(any(), any()) } answers { AppStatus.Error }

        val viewModel = appConfigViewModel()
        viewModel.refresh(mobileCoreWrapper)

        Assert.assertEquals(viewModel.appStatusLiveData.value, AppStatus.Error)
    }

    @Test
    fun `refresh with no config files in verifier app emits internet required status`() = runBlocking {
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
        coEvery { appConfigStorageManager.areConfigFilesPresentInFilesFolder() } returns false

        val viewModel = appConfigViewModel(true)
        viewModel.refresh(mobileCoreWrapper)

        Assert.assertEquals(viewModel.appStatusLiveData.value, AppStatus.Error)
    }
}
