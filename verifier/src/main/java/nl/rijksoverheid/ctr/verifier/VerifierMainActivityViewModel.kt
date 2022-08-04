package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.scanlog.usecase.ScanLogsCleanupUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class VerifierMainActivityViewModel : ViewModel() {
    val isPolicyUpdatedLiveData: LiveData<Event<Boolean>> = MutableLiveData()
    abstract fun cleanup()
    abstract fun policyUpdate()
}

class VerifierMainActivityViewModelImpl(
    private val scanLogsCleanupUseCase: ScanLogsCleanupUseCase,
    private val configVerificationPolicyUseCase: ConfigVerificationPolicyUseCase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : VerifierMainActivityViewModel() {

    override fun cleanup() {
        viewModelScope.launch {
            scanLogsCleanupUseCase.cleanup()
        }
    }

    override fun policyUpdate() {
        val appConfig = cachedAppConfigUseCase.getCachedAppConfig()
        if (!appConfig.appDeactivated) {
            viewModelScope.launch {
                val isPolicyUpdated = configVerificationPolicyUseCase.updatePolicy()
                (isPolicyUpdatedLiveData as MutableLiveData).postValue(Event(isPolicyUpdated))
            }
        }
    }
}
