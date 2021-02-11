package nl.rijksoverheid.ctr.verifier.status

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.models.AppStatus
import nl.rijksoverheid.ctr.shared.models.ConfigType
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.shared.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.verifier.BuildConfig

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class StatusViewModel(
    private val appStatusUseCase: AppStatusUseCase
) : ViewModel() {

    val appStatusLiveData = MutableLiveData<Result<AppStatus>>()

    fun getAppStatus() {
        appStatusLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val appStatus =
                    appStatusUseCase.status(BuildConfig.VERSION_CODE, ConfigType.Verifier)
                withContext(Dispatchers.Main) {
                    appStatusLiveData.value = Result.Success(appStatus)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appStatusLiveData.value = Result.Failed(e)
                }
            }
        }
    }
}
