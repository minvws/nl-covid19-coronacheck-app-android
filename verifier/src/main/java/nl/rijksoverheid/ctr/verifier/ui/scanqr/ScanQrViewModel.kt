package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class ScanQrViewModel : ViewModel() {
    val scannerStateLiveData: LiveData<Event<ScannerState>> = MutableLiveData()
    val scannerNavigationStateEvent: LiveData<Event<ScannerNavigationState>> = MutableLiveData()
    abstract fun hasSeenScanInstructions(): Boolean
    abstract fun setScanInstructionsSeen()
    abstract fun checkPolicyUpdate()
    abstract fun nextScreen()
}

class ScanQrViewModelImpl(
    private val persistenceManager: PersistenceManager,
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase,
    private val scannerStateUseCase: ScannerStateUseCase,
    private val policyUseCase: ConfigVerificationPolicyUseCase
) : ScanQrViewModel() {

    override fun hasSeenScanInstructions(): Boolean {
        return persistenceManager.getScanInstructionsSeen()
    }

    override fun setScanInstructionsSeen() {
        if (!hasSeenScanInstructions()) {
            persistenceManager.setScanInstructionsSeen()
        }
    }

    override fun checkPolicyUpdate() {
        viewModelScope.launch {
            policyUseCase.updatePolicy()
            (scannerStateLiveData as MutableLiveData).postValue(
                Event(scannerStateUseCase.get())
            )
        }
    }

    override fun nextScreen() {
        val nextScreenState = scannerNavigationStateUseCase.get()
        (scannerNavigationStateEvent as MutableLiveData).postValue(Event(nextScreenState))
    }
}
