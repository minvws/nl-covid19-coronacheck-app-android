/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.usecases.DeleteConfigUseCase

abstract class AppConfigViewModel : ViewModel() {
    val appStatusLiveData = MutableLiveData<AppStatus>()
}

class AppConfigViewModelImpl(
    private val deleteConfigUseCase: DeleteConfigUseCase
) : AppConfigViewModel() {

    init {
        viewModelScope.launch {
            try {
                deleteConfigUseCase()
            } catch (exception: Exception) {
                // no op
            }
        }
    }
}
