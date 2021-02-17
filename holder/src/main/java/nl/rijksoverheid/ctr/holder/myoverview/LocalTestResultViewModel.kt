package nl.rijksoverheid.ctr.holder.myoverview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class LocalTestResultViewModel(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val localTestResultUseCase: LocalTestResultUseCase
) : ViewModel() {

    val localTestResultLiveData = MutableLiveData<Event<LocalTestResult>>()

    val retrievedLocalTestResult: LocalTestResult?
        get() = localTestResultLiveData.value?.peekContent()

    fun getLocalTestResult(currentDateTime: OffsetDateTime) {
        viewModelScope.launch {
            secretKeyUseCase.persist()
            val localTestResult = localTestResultUseCase.get()
            localTestResult?.let {
                localTestResultLiveData.value = Event(localTestResult)
            }
        }
    }
}
