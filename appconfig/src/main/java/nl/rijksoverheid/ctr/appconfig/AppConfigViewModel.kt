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
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecase.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecase.PersistConfigUseCase

class AppConfigViewModel(
    private val appConfigUseCase: AppConfigUseCase,
    private val appStatusUseCase: AppStatusUseCase,
    private val persistConfigUseCase: PersistConfigUseCase,
    private val loadPublicKeysUseCase: LoadPublicKeysUseCase,
    private val versionCode: Int
) : ViewModel() {

    val appStatusLiveData = MutableLiveData<AppStatus>()

    fun refresh() {
        viewModelScope.launch {
            val configResult = appConfigUseCase.get()
            val appStatus = appStatusUseCase.get(configResult, versionCode)
            if (configResult is ConfigResult.Success) {
                persistConfigUseCase.persist(
                    appConfig = configResult.appConfig,
                    publicKeys = configResult.publicKeys
                )
                loadPublicKeysUseCase.load(
                    publicKeys = configResult.publicKeys
                )
            }
            appStatusLiveData.postValue(appStatus)
        }
    }
}
