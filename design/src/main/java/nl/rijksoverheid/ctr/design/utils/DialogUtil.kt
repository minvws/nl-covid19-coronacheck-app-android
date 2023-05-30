package nl.rijksoverheid.ctr.design.utils

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.R

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
        @StringRes title: Int? = null,
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
        @StringRes title: Int?,
        message: String,
        @StringRes positiveButtonText: Int,
        positiveButtonCallback: () -> Unit,
        @StringRes negativeButtonText: Int?,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        val builder = MaterialAlertDialogBuilder(context, R.style.App_Dialog_DayNight)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                positiveButtonCallback()
                dialog.dismiss()
            }

        title?.let {
            builder.setTitle(title)
        }

        negativeButtonText?.let {
            builder.setNegativeButton(it) { dialog, _ ->
                negativeButtonCallback?.invoke()
                dialog.dismiss()
            }
        }

        onDismissCallback?.let {
            builder.setOnDismissListener {
                onDismissCallback()
            }
        }

        builder.create().show()
    }
}
