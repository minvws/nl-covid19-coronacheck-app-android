package nl.rijksoverheid.ctr.design.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DialogUtil {
    fun presentDialog(
        context: Context,
        @StringRes title: Int,
        message: String,
        @StringRes positiveButtonText: Int,
        positiveButtonCallback: () -> Unit,
        @StringRes negativeButtonText: Int? = null,
        negativeButtonCallback: (() -> Unit)? = null,
        onDismissCallback: (() -> Unit)? = null
    )
}

class DialogUtilImpl : DialogUtil {

    override fun presentDialog(
        context: Context,
        @StringRes title: Int,
        message: String,
        @StringRes positiveButtonText: Int,
        positiveButtonCallback: () -> Unit,
        @StringRes negativeButtonText: Int?,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        if (fragmentManager.findFragmentByTag(DialogFragment.TAG) == null) {
            DialogFragment().show(
                manager = fragmentManager,
                tag = DialogFragment.TAG,
                title = context.getString(title),
                message = message,
                positiveButtonText = context.getString(positiveButtonText),
                positiveButtonCallback = positiveButtonCallback,
                negativeButtonText = if (negativeButtonText != null) {
                    context.getString(negativeButtonText)
                } else {
                    null
                },
                negativeButtonCallback = negativeButtonCallback,
                onDismissCallback = onDismissCallback,
            )
        }
    }
}
