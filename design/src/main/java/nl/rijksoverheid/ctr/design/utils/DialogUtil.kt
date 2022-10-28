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

    fun dismiss(context: Context?)
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
        val activity = context as FragmentActivity
        val fragmentManager = context.supportFragmentManager
        if (fragmentManager.findFragmentByTag(DialogFragment.TAG) == null) {
            val dialog = DialogFragment(title, message, positiveButtonText, negativeButtonText)
            fragmentManager.setFragmentResultListener(
                DialogFragment.KEY_DIALOG_RESULT,
                activity
            ) { _, bundle ->
                fragmentManager.clearFragmentResultListener(DialogFragment.KEY_DIALOG_RESULT)
                when {
                    bundle.getBoolean(DialogFragment.KEY_SHOWN) -> {
                        if ((dialog as? DialogFragment)?.isAdded == true) {
                            dialog.registerCallbacks(
                                positiveButtonText,
                                negativeButtonText,
                                positiveButtonCallback,
                                negativeButtonCallback,
                                onDismissCallback
                            )
                        }
                    }
                }
            }
            dialog.show(fragmentManager, DialogFragment.TAG)
        }
    }

    override fun dismiss(context: Context?) {
        val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
        fragmentManager?.clearFragmentResultListener(DialogFragment.KEY_DIALOG_RESULT)
        (fragmentManager?.findFragmentByTag(DialogFragment.TAG) as? DialogFragment)?.dismiss()
    }
}
