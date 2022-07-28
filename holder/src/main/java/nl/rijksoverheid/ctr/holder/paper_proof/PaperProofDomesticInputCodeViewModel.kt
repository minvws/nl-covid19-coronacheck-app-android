/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.paper_proof

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticCodeResult
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticResult
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticInputCodeUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PaperProofDomesticInputCodeViewModel : ViewModel() {
    val loading: LiveData<Boolean> = MutableLiveData()
    val validateCodeLiveData: LiveData<Event<PaperProofDomesticCodeResult>> = MutableLiveData()
    val validateProofLiveData: LiveData<Event<PaperProofDomesticResult>> = MutableLiveData()

    abstract fun validateCode(code: String)
    abstract fun validateProof(qrContent: String, couplingCode: String)
}

class PaperProofDomesticInputCodeViewModelImpl(
    private val validatePaperProofDomesticInputCodeUseCase: ValidatePaperProofDomesticInputCodeUseCase,
    private val validatePaperProofDomesticUseCase: ValidatePaperProofDomesticUseCase
) : PaperProofDomesticInputCodeViewModel() {

    override fun validateCode(code: String) {
        val result = validatePaperProofDomesticInputCodeUseCase.validate(code)
        (validateCodeLiveData as MutableLiveData).postValue(Event(result))
    }

    override fun validateProof(qrContent: String, couplingCode: String) {
        viewModelScope.launch {
            (loading as MutableLiveData).postValue(true)
            val result = validatePaperProofDomesticUseCase.validate(
                qrContent = qrContent,
                couplingCode = couplingCode
            )
            (validateProofLiveData as MutableLiveData).postValue(Event(result))
            loading.postValue(false)
        }
    }
}
