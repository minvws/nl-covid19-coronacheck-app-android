package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySwitchState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class ScanQrViewModel : ViewModel() {
    val liveData: LiveData<Event<ScanQRState>> = MutableLiveData()
    val startupStateEvent: LiveData<Event<ScannerNavigationState>> = MutableLiveData()
    abstract fun hasSeenScanInstructions(): Boolean
    abstract fun setScanInstructionsSeen()
    abstract fun getNextScannerScreenState(): ScannerNavigationState
    abstract fun onViewCreated()
    abstract fun nextScreen()
}

class ScanQrViewModelImpl(
    private val persistenceManager: PersistenceManager,
    private val useCase: VerificationPolicyUseCase,
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase,
) : ScanQrViewModel() {

    override fun hasSeenScanInstructions(): Boolean {
        return persistenceManager.getScanInstructionsSeen()
    }

    override fun setScanInstructionsSeen() {
        if (!hasSeenScanInstructions()) {
            persistenceManager.setScanInstructionsSeen()
        }
    }

    override fun getNextScannerScreenState(): ScannerNavigationState {
        return scannerNavigationStateUseCase.get()
    }

    override fun onViewCreated() {
        (liveData as MutableLiveData).postValue(
            Event(ScanQRState(
                policy = useCase.getState(),
                lock = useCase.getSwitchState(),
            ))
        )
    }

    override fun nextScreen() {
        val nextScreenState = getNextScannerScreenState()
        val isScannerUnlocked = useCase.getSwitchState() !is VerificationPolicySwitchState.Locked
        if (isScannerUnlocked ||
            (nextScreenState !is ScannerNavigationState.Scanner)
        ) {
            (startupStateEvent as MutableLiveData).postValue(Event(getNextScannerScreenState()))
        }
    }
}
