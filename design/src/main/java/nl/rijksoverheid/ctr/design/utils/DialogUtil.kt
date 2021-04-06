package nl.rijksoverheid.ctr.design.utils

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        @StringRes message: Int,
        @StringRes positiveButtonText: Int,
        positiveButtonCallback: () -> Unit,
        @StringRes negativeButtonText: Int? = null,
        negativeButtonCallback: (() -> Unit)? = null
    )
}

class DialogUtilImpl : DialogUtil {

    override fun presentDialog(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButtonText: Int,
        positiveButtonCallback: () -> Unit,
        @StringRes negativeButtonText: Int?,
        negativeButtonCallback: (() -> Unit)?
    ) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                positiveButtonText
            ) { _, _ -> positiveButtonCallback.invoke() }

        if (negativeButtonText != null) {
            builder.setNegativeButton(
                negativeButtonText
            ) { _, _ -> negativeButtonCallback?.invoke() }
        }

        builder.show()
    }
}
