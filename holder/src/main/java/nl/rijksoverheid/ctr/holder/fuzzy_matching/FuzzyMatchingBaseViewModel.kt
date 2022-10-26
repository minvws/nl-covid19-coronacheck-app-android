package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class FuzzyMatchingBaseViewModel(
    private val holderDatabase: HolderDatabase,
    private val greenCardUtil: GreenCardUtil
) : ViewModel() {
    val canSkipLiveData: LiveData<Boolean> = MutableLiveData()
    fun canSkip(fromGetEvents: Boolean) {
        if (fromGetEvents) {
            (canSkipLiveData as MutableLiveData).value = false
        } else {
            viewModelScope.launch {
                val activeCredentialExists = holderDatabase.greenCardDao().getAll()
                    .any { !greenCardUtil.hasNoActiveCredentials(it) }
                (canSkipLiveData as MutableLiveData).value = activeCredentialExists
            }
        }
    }
}
