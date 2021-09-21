/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.PersistConfigUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.ClmobileVerifyException
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class AppConfigViewModel : ViewModel() {
    val appStatusLiveData = MutableLiveData<Event<AppStatus>>()

    abstract fun refresh(mobileCoreWrapper: MobileCoreWrapper)
}

class AppConfigViewModelImpl(
    private val appConfigUseCase: AppConfigUseCase,
    private val appStatusUseCase: AppStatusUseCase,
    private val persistConfigUseCase: PersistConfigUseCase,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val filesDirPath: String,
    private val isVerifierApp: Boolean,
    private val versionCode: Int
) : AppConfigViewModel() {

    private val mutex = Mutex()

    override fun refresh(mobileCoreWrapper: MobileCoreWrapper) {
        viewModelScope.launch {
            // allow only one config/public keys refresh at a time
            // cause we store them writing to files and a parallel
            // operation could break them eventually
            mutex.withLock {
                val configResult = appConfigUseCase.get()
                val appStatus = appStatusUseCase.get(configResult, versionCode)
                if (configResult is ConfigResult.Success) {
                    persistConfigUseCase.persist(
                        appConfigContents = configResult.appConfig,
                        publicKeyContents = configResult.publicKeys
                    )
                }

                val configFilesArePresentInFilesFolder =
                    appConfigStorageManager.areConfigFilesPresentInFilesFolder()
                if (!configFilesArePresentInFilesFolder || !cachedAppConfigUseCase.isCachedAppConfigValid()) {
                    return@launch appStatusLiveData.postValue(Event(AppStatus.Error))
                }

                val initializationError = if (isVerifierApp) {
                    mobileCoreWrapper.initializeVerifier(filesDirPath)
                } else {
                    mobileCoreWrapper.initializeHolder(filesDirPath)
                }

                if (initializationError != null) {
                    throw ClmobileVerifyException(initializationError)
                }

                appStatusLiveData.postValue(Event(appStatus))
            }
        }
    }
}
