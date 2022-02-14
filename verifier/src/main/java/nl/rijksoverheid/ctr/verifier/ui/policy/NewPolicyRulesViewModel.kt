/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationStateUseCase

abstract class NewPolicyRulesViewModel : ViewModel() {

    val scannerNavigationStateEvent: LiveData<Event<ScannerNavigationState>> = MutableLiveData()
    val newPolicyRules: LiveData<NewPolicyItem> = MutableLiveData()
    abstract fun init()
    abstract fun nextScreen()
}

class NewPolicyRulesViewModelImpl(
    private val persistenceManager: PersistenceManager,
    private val newPolicyRulesItemUseCase: NewPolicyRulesItemUseCase,
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase,
) : NewPolicyRulesViewModel() {

    override fun init() {
        val policyItem = newPolicyRulesItemUseCase.get()
        (newPolicyRules as MutableLiveData).postValue(policyItem)
    }

    override fun nextScreen() {
        persistenceManager.setNewPolicyRulesSeen(true)

        val nextScreenState = scannerNavigationStateUseCase.get()
        (scannerNavigationStateEvent as MutableLiveData).postValue(Event(nextScreenState))
    }
}