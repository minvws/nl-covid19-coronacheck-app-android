package nl.rijksoverheid.ctr.design.utils

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat

@Parcelize
data class DialogFragmentData(
    val title: Int? = null,
    val message: Int? = null,
    val text: String? = null,
    val messageArguments: List<String>? = null,
    val positiveButtonData: DialogButtonData,
    val negativeButtonData: DialogButtonData? = null
) : Parcelable

class SharedDialogFragment : DialogFragment() {
    companion object {
        const val argumentsDataKey = "data"
        private const val tag = "shared_dialog_fragment"

        fun show(fragmentManager: FragmentManager, data: DialogFragmentData) {
            val args = Bundle().apply {
                putParcelable(argumentsDataKey, data)
            }
            SharedDialogFragment().apply { arguments = args }.show(fragmentManager, tag)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.getParcelableCompat<DialogFragmentData>(argumentsDataKey)?.let {
            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.App_Dialog_DayNight)

            it.title?.let { title ->
                builder.setTitle(title)
            }

            it.message?.let { message ->
                builder.setMessage(message)
                it.messageArguments?.toTypedArray()?.let { args ->
                    builder.setMessage(getString(it.message, *args))
                }
            }

            it.text?.let { text ->
                builder.setMessage(text)
            }

            it.positiveButtonData.apply {
                builder.setPositiveButton(textId) { dialog, _ ->
                    when (this) {
                        is DialogButtonData.NavigationButton -> findNavControllerSafety()?.navigate(
                            navigationActionId,
                            navigationArguments
                        )

                        is DialogButtonData.ResetApp -> {
                            (context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.clearApplicationUserData()
                        }

                        is DialogButtonData.Dismiss -> {
                            findNavController().navigateUp()
                        }
                    }
                    dialog.dismiss()
                }
            }

            it.negativeButtonData?.apply {
                builder.setNegativeButton(textId) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            return builder.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
}
