package nl.rijksoverheid.ctr.holder

import android.app.ProgressDialog
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    // TODO: Make non blocking loaders
    val progressDialog by lazy {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading")
        progressDialog
    }

    // TODO: Make non blocking loaders
    fun presentLoading(present: Boolean) {
        if (present) {
            progressDialog.show()
        } else if (progressDialog.isShowing && !present) {
            progressDialog.hide()
        }
    }

    fun presentError(
        titleRes: Int = R.string.dialog_error_title,
        messageRes: Int = R.string.dialog_error_message
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(titleRes))
            .setMessage(getString(messageRes))
            .setPositiveButton(R.string.ok) { dialog, which -> }
            .show()
    }

}
