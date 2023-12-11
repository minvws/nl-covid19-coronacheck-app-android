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
import java.net.UnknownHostException
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ConfigResultUseCase
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.factories.OnboardingFlow

abstract class AppConfigViewModel : ViewModel() {
    val appStatusLiveData = MutableLiveData<AppStatus>()

    abstract fun refresh(
        force: Boolean = false,
        afterRefresh: () -> Unit = {}
    )
    abstract fun saveNewFeaturesFinished()
    abstract fun saveNewTerms()
}

class AppConfigViewModelImpl(
    private val appConfigUseCase: AppConfigUseCase,
    private val appStatusUseCase: AppStatusUseCase,
    private val configResultUseCase: ConfigResultUseCase,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val filesDirPath: String,
    private val isVerifierApp: Boolean,
    private val versionCode: Int,
    private val appUpdatePersistenceManager: AppUpdatePersistenceManager,
    private val errorCodeStringFactory: ErrorCodeStringFactory,
    private val appUpdateData: AppUpdateData
) : AppConfigViewModel() {

    private fun updateAppStatus(appStatus: AppStatus) {
        if (appStatusLiveData.value != appStatus) {
            appStatusLiveData.postValue(appStatus)
        }
    }

    override fun refresh(
        force: Boolean,
        afterRefresh: () -> Unit
    ) {
        // update the app status from the last fetched config
        // only if it is valid (so don't use the default one)
        if (cachedAppConfigUseCase.isCachedAppConfigValid()) {
            val appStatus = appStatusUseCase.checkIfActionRequired(
                versionCode,
                cachedAppConfigUseCase.getCachedAppConfig()
            )
            updateAppStatus(appStatus)
        }

        if (!force && !appConfigUseCase.canRefresh(cachedAppConfigUseCase)) {
            return
        }
        viewModelScope.launch {
            val configResult = configResultUseCase.fetch()
            afterRefresh()
            val appStatus = appStatusUseCase.get(configResult, versionCode)

            val configFilesArePresentInFilesFolder =
                appConfigStorageManager.areConfigFilesPresentInFilesFolder()
            if (!configFilesArePresentInFilesFolder || !cachedAppConfigUseCase.isCachedAppConfigValid()) {
                if (configResult is ConfigResult.Error && configResult.error.e !is UnknownHostException) {
                    return@launch appStatusLiveData.postValue(
                        AppStatus.LaunchError(
                            errorCodeStringFactory.get(
                                OnboardingFlow,
                                listOf(configResult.error)
                            )
                        )
                    )
                } else {
                    return@launch appStatusLiveData.postValue(AppStatus.Error)
                }
            }

            updateAppStatus(appStatus)
        }
    }

    override fun saveNewFeaturesFinished() {
        appUpdateData.newFeatureVersion?.let { appUpdatePersistenceManager.saveNewFeaturesSeen(it) }
        updateAppStatus(
            appStatusUseCase.checkIfActionRequired(
                versionCode,
                cachedAppConfigUseCase.getCachedAppConfig()
            )
        )
    }

    override fun saveNewTerms() {
        appUpdatePersistenceManager.saveNewTermsSeen(appUpdateData.newTerms.version)
        updateAppStatus(
            appStatusUseCase.checkIfActionRequired(
                versionCode,
                cachedAppConfigUseCase.getCachedAppConfig()
            )
        )
    }
}
