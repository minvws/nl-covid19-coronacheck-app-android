package nl.rijksoverheid.ctr.holder.ui.priority_notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class PriorityNotificationViewModel : ViewModel() {
    val showPriorityNotificationLiveData = MutableLiveData<Event<String>>()
}

class PriorityNotificationViewModelImpl(
    private val priorityNotificationUseCase: PriorityNotificationUseCase
) : PriorityNotificationViewModel() {
    init {
        check()
    }

    fun check() = viewModelScope.launch {
        val priorityNotificationMessage = priorityNotificationUseCase.get()
        if (!priorityNotificationMessage.isNullOrEmpty()) {
            showPriorityNotificationLiveData.postValue(Event(priorityNotificationMessage))
        }
    }
}
