package nl.rijksoverheid.ctr.holder.status

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.usecase.OnboardingUseCase
import nl.rijksoverheid.ctr.shared.models.AppStatus
import nl.rijksoverheid.ctr.shared.models.ConfigType
import nl.rijksoverheid.ctr.shared.models.Result
import nl.rijksoverheid.ctr.shared.usecases.AppStatusUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class StatusViewModel(
    private val onboardingUseCase: OnboardingUseCase,
    private val appStatusUseCase: AppStatusUseCase
) : ViewModel() {

    val appStatusLiveData: MutableLiveData<Result<AppStatus>> = MutableLiveData()
    val onboardingFinishedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getAppStatus() {
        appStatusLiveData.value = Result.Loading()
        viewModelScope.launch {
            try {
                val appStatus = appStatusUseCase.status(BuildConfig.VERSION_CODE, ConfigType.Holder)
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

    fun getOnboardingFinished() {
        val onboardingFinished = onboardingUseCase.onboardingFinished()
        onboardingFinishedLiveData.postValue(onboardingFinished)
    }

    fun setOnboardingFinished() = onboardingUseCase.setOnboardingFinished()
}
