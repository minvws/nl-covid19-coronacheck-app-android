package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase.ScanLogsCleanupUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class VerifierMainActivityViewModel: ViewModel() {
    abstract fun cleanup()
}

class VerifierMainActivityViewModelImpl(
    private val scanLogsCleanupUseCase: ScanLogsCleanupUseCase): VerifierMainActivityViewModel() {

    override fun cleanup() {
        viewModelScope.launch {
            scanLogsCleanupUseCase.cleanup()
        }
    }
}

