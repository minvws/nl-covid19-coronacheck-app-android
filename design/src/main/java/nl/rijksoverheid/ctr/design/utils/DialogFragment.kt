package nl.rijksoverheid.ctr.design.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    private val viewModel: DialogViewModel by viewModel()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismissCallbackLiveData.value?.invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.onDismissCallbackLiveData.observe(this) { onDismiss ->
            (dialog as? AlertDialog)?.setOnDismissListener {
                dismiss()
            }
        }
        viewModel.negativeButtonCallbackLiveData.observe(this) { (negativeButtonText, onCancel) ->
            negativeButtonText?.let {
                (dialog as? AlertDialog)?.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    getString(negativeButtonText)
                ) { _, _ ->
                    onCancel?.invoke()
                    dismiss()
                }
            }
        }
        viewModel.positiveButtonCallbackLiveData.observe(this) { (positiveButtonText, onOk) ->
            (dialog as? AlertDialog)?.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(positiveButtonText)
            ) { _, _ ->
                onOk.invoke()
                dismiss()
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireNotNull(arguments?.title))
            .setMessage(requireNotNull(arguments?.message))
            .setPositiveButton(requireNotNull(arguments?.positiveButtonText), null)
        arguments?.negativeButtonText?.let {
            builder.setNegativeButton(it, null)
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        setFragmentResult(KEY_DIALOG_RESULT, bundleOf(KEY_SHOWN to true))
        super.onAttach(context)
    }

    fun registerCallbacks(
        positiveButtonText: Int,
        negativeButtonText: Int?,
        positiveButtonCallback: () -> Unit,
        negativeButtonCallback: (() -> Unit)?,
        onDismissCallback: (() -> Unit)?
    ) {
        viewModel.registerButtonCallbacks(
            positiveButtonText,
            negativeButtonText,
            positiveButtonCallback,
            negativeButtonCallback,
            onDismissCallback
        )
    }

    companion object {
        internal const val TAG = "DialogFragment"
        internal const val KEY_DIALOG_RESULT = "dialog_result"
        internal const val KEY_SHOWN = "shown"

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
