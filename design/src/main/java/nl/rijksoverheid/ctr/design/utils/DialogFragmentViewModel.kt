package nl.rijksoverheid.ctr.design.utils

import androidx.lifecycle.ViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DialogFragmentViewModel: ViewModel() {

    abstract var title: String
    abstract var message: String
    abstract var positiveButtonText: String
    abstract var positiveButtonCallback: () -> Unit
    abstract var negativeButtonText: String?
    abstract var negativeButtonCallback: (() -> Unit)?
    abstract var onDismissCallback: (() -> Unit)?

    abstract fun show(
        title: String,
        message: String,
        positiveButtonText: String,
        positiveButtonCallback: () -> Unit,
        negativeButtonText: String? = null,
        negativeButtonCallback: (() -> Unit)? = null,
        onDismissCallback: (() -> Unit)? = null
    )

}

/**
 * The dialog fragment needs a view model to hold all the information needed on screen rotation.
 * The fragment manager recreates the dialog fragment calling its constructor without arguments,
 * so we cannot have a custom constructor.
 * Instead we save here our data and the dialog listener methods (which cannot be serialised), so
 * they can be used on the fragment recreation after a screen rotation.
 */
class DialogFragmentViewModelImpl: DialogFragmentViewModel() {
    
    override lateinit var title: String
    override lateinit var message: String
    override lateinit var positiveButtonText: String
    override lateinit var positiveButtonCallback: () -> Unit
    override var negativeButtonText: String? = null
    override var negativeButtonCallback: (() -> Unit)? = null
    override var onDismissCallback: (() -> Unit)? = null

    override fun show(
        title: String,
        message: String,
        positiveButtonText: String,
        positiveButtonCallback: () -> Unit,
        negativeButtonText: String?,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        negativeButtonCallback?.run {  }
        this.title = title
        this.message = message
        this.positiveButtonText = positiveButtonText
        this.positiveButtonCallback = positiveButtonCallback
        this.negativeButtonText = negativeButtonText
        this.negativeButtonCallback = negativeButtonCallback
        this.onDismissCallback = onDismissCallback
    }
}
