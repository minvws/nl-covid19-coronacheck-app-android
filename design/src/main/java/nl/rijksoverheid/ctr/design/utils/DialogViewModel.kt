package nl.rijksoverheid.ctr.design.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DialogViewModel : ViewModel() {
    // emits a pair of the positive button string resource id and its click listener
    val positiveButtonCallbackLiveData: LiveData<Pair<Int, () -> Unit>> = MutableLiveData()
    // emits a pair of the negative button string resource id and its click listener
    val negativeButtonCallbackLiveData: LiveData<Pair<Int?, (() -> Unit)?>> = MutableLiveData()
    // emits the callback for the dialog's dismiss listener
    val onDismissCallbackLiveData: LiveData<(() -> Unit)?> = MutableLiveData()

    abstract fun registerButtonCallbacks(
        positiveButtonText: Int,
        negativeButtonText: Int?,
        positiveButtonCallback: () -> Unit,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    )

    abstract fun onDestroy()
}

class DialogViewModelImpl : DialogViewModel() {
    override fun registerButtonCallbacks(
        positiveButtonText: Int,
        negativeButtonText: Int?,
        positiveButtonCallback: () -> Unit,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        (positiveButtonCallbackLiveData as MutableLiveData).value = positiveButtonText to positiveButtonCallback
        (negativeButtonCallbackLiveData as MutableLiveData).value = negativeButtonText to negativeButtonCallback
        (onDismissCallbackLiveData as MutableLiveData).value = onDismissCallback
    }

    override fun onDestroy() {
        (onDismissCallbackLiveData as MutableLiveData).value = null
    }
}
