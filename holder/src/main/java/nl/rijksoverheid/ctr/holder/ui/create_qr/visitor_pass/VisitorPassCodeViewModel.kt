package nl.rijksoverheid.ctr.holder.ui.create_qr.visitor_pass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.PaperProofCodeResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.PaperProofCodeUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class VisitorPassCodeViewModel : ViewModel() {
    val codeResultLiveData: LiveData<Event<PaperProofCodeResult>> = MutableLiveData()
    abstract fun validateCode(code: String)
}

class VisitorPassCodeViewModelImpl(private val paperProofCodeUseCase: PaperProofCodeUseCase): VisitorPassCodeViewModel() {

    override fun validateCode(code: String) {
        (codeResultLiveData as MutableLiveData).postValue(Event(paperProofCodeUseCase.validate(code)))
    }
}