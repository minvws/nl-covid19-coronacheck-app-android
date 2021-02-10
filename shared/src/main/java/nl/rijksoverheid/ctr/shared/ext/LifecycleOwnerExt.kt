package nl.rijksoverheid.ctr.shared.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import nl.rijksoverheid.ctr.shared.models.Result

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
inline fun <O : Any> LifecycleOwner.observeResult(
    liveData: LiveData<Result<O>>,
    crossinline loading: () -> Unit,
    crossinline success: (O) -> Unit,
    crossinline error: (Exception?) -> Unit
) {

    liveData.observe(this, Observer {
        when (it) {
            is Result.Loading -> {
                loading.invoke()
            }
            is Result.Success -> {
                success.invoke(it.data)
            }
            is Result.Failed -> {
                error.invoke(it.e)
            }
        }
    })
}
