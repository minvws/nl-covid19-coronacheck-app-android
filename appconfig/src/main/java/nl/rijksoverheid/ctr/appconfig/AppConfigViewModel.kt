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
import nl.rijksoverheid.ctr.appconfig.usecase.AppConfigUseCase

class AppConfigViewModel(
    private val appStatusUseCase: AppConfigUseCase,
    private val versionCode: Int
) : ViewModel() {

    private val refresh = MutableLiveData(Unit)

    val appStatus = refresh.switchMap {
        liveData {
            emit(appStatusUseCase.config(versionCode))
        }
    }

    fun refresh() {
        refresh.value = Unit
    }
}
