package nl.rijksoverheid.ctr.design.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DialogFragment : DialogFragment() {

    private val viewModel: DialogFragmentViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.isInitialised()) {
            // dismiss after process death since view model is lost
            // and we have no data for the dialog builder
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(viewModel.title)
            .setMessage(viewModel.message)
            .setPositiveButton(
                viewModel.positiveButtonText
            ) { _, _ ->
                dismiss()
                viewModel.positiveButtonCallback.invoke()
            }

        if (viewModel.negativeButtonText != null) {
            builder.setNegativeButton(
                viewModel.negativeButtonText
            ) { _, _ ->
                dismiss()
                viewModel.negativeButtonCallback?.invoke()
            }
        }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismissCallback?.invoke()
    }

    fun show(
        manager: FragmentManager,
        tag: String?,
        title: String,
        message: String,
        positiveButtonText: String,
        positiveButtonCallback: () -> Unit,
        negativeButtonText: String?,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        super.show(manager, tag)
        viewModel.show(title, message, positiveButtonText, positiveButtonCallback, negativeButtonText, negativeButtonCallback, onDismissCallback)
    }

    companion object {
        const val TAG = "DialogFragment"
    }
}
