package nl.rijksoverheid.ctr.design.utils

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat

@Parcelize
data class DialogFragmentData(
    val title: Int,
    val message: Int,
    val messageArguments: List<String>? = null,
    val positiveButtonData: DialogButtonData,
    val negativeButtonData: DialogButtonData? = null
) : Parcelable

class SharedDialogFragment : DialogFragment() {
    companion object {
        const val argumentsDataKey = "data"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.getParcelableCompat<DialogFragmentData>(argumentsDataKey)?.let {
            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.App_Dialog_DayNight)
                .setTitle(it.title)
                .setMessage(it.message)

            it.messageArguments?.toTypedArray()?.let { args ->
                builder.setMessage(getString(it.message, *args))
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
                            dialog.dismiss()
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
