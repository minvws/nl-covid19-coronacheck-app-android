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
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap

class AppStatusViewModel(
    private val appStatusUseCase: AppStatusUseCase,
    private val versionCode: Int
) : ViewModel() {

    private val refresh = MutableLiveData(Unit)

    val appStatus = refresh.switchMap {
        liveData {
            emit(appStatusUseCase.status(versionCode))
        }
    }

    fun refresh() {
        refresh.value = Unit
    }
}