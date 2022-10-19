package nl.rijksoverheid.ctr.design.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

private const val KEY_DIALOG_TITLE = "title"
private const val KEY_DIALOG_MESSAGE = "message"
private const val KEY_POSITIVE_BUTTON_TEXT = "positive_button_text"
private const val KEY_NEGATIVE_BUTTON_TEXT = "negative_button_text"

private var Bundle.title: Int
    get() = getInt(KEY_DIALOG_TITLE)
    set(value) {
        putInt(KEY_DIALOG_TITLE, value)
    }

private var Bundle.message: String
    get() = getString(KEY_DIALOG_MESSAGE, "")
    set(value) {
        putString(KEY_DIALOG_MESSAGE, value)
    }

private var Bundle.positiveButtonText: Int
    get() = getInt(KEY_POSITIVE_BUTTON_TEXT)
    set(value) {
        putInt(KEY_POSITIVE_BUTTON_TEXT, value)
    }

private var Bundle.negativeButtonText: Int?
    get() = getInt(KEY_NEGATIVE_BUTTON_TEXT, 0).takeIf { it != 0 }
    set(value) {
        putInt(KEY_NEGATIVE_BUTTON_TEXT, value ?: 0)
    }

class DialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireNotNull(arguments?.title))
            .setMessage(requireNotNull(arguments?.message))
            .setPositiveButton(
                requireNotNull(arguments?.positiveButtonText)
            ) { _, _ ->
                setFragmentResult(KEY_DIALOG_RESULT, bundleOf(KEY_POSITIVE to true))
                dismiss()
            }

        if (arguments?.negativeButtonText != null) {
            builder.setNegativeButton(
                requireNotNull(arguments?.negativeButtonText)
            ) { _, _ ->
                setFragmentResult(KEY_DIALOG_RESULT, bundleOf(KEY_NEGATIVE to true))
                dismiss()
            }
        }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        setFragmentResult(KEY_DIALOG_RESULT, bundleOf(KEY_DISMISSED to true))
        super.onDismiss(dialog)
    }

    companion object {
        internal const val TAG = "DialogFragment"
        internal const val KEY_DIALOG_RESULT = "dialog_result"
        internal const val KEY_POSITIVE = "positive"
        internal const val KEY_NEGATIVE = "negative"
        internal const val KEY_DISMISSED = "dismissed"

        internal operator fun invoke(
            @StringRes title: Int,
            message: String,
            @StringRes positiveButtonText: Int,
            @StringRes negativeButtonText: Int?
        ): nl.rijksoverheid.ctr.design.utils.DialogFragment {
            return nl.rijksoverheid.ctr.design.utils.DialogFragment().apply {
                arguments = Bundle().apply {
                    this.title = title
                    this.message = message
                    this.positiveButtonText = positiveButtonText
                    this.negativeButtonText = negativeButtonText
                }
            }
        }
    }
}
