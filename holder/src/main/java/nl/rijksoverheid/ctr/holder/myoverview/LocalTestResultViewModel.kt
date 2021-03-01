package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class LocalTestResultViewModel : ViewModel() {

    val localTestResultStateLiveData = MutableLiveData<Event<LocalTestResultState>>()
    val retrievedLocalTestResult: LocalTestResult?
        get() = (localTestResultStateLiveData.value?.peekContent() as? LocalTestResultState.Valid)?.localTestResult

    abstract fun getLocalTestResult()
}

open class LocalTestResultViewModelImpl(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val localTestResultUseCase: LocalTestResultUseCase
) : LocalTestResultViewModel() {

    override fun getLocalTestResult() {
        viewModelScope.launch {
            secretKeyUseCase.persist()
            val localTestResultState = localTestResultUseCase.get()
            localTestResultStateLiveData.value = Event(localTestResultState)
        }
    }
}
