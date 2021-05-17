package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventUseCase
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class VaccinationViewModel : ViewModel() {
    abstract fun getEvents(digidToken: String)
}

class VaccinationViewModelImpl(private val eventUseCase: EventUseCase) : VaccinationViewModel() {
    override fun getEvents(digidToken: String) {
        viewModelScope.launch {
            val result = eventUseCase.getEvents(digidToken)
            Timber.v("VACFLOW: Fetched test providers: $result")
        }
    }

}
