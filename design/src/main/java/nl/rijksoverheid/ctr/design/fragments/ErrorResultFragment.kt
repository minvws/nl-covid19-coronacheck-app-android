package nl.rijksoverheid.ctr.design.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentErrorResultBinding
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import org.koin.android.ext.android.inject

/**
 * Fragment to show errors
 */
class ErrorResultFragment : Fragment(R.layout.fragment_error_result) {

    private val intentUtil: IntentUtil by inject()

    companion object {

        private const val EXTRA_DATA = "EXTRA_DATA"

        fun getBundle(data: ErrorResultFragmentData): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_DATA, data)
            return bundle
        }
    }

    private val data by lazy {
        arguments?.getParcelableCompat<ErrorResultFragmentData>(EXTRA_DATA)
            ?: error("ErrorResultFragmentData cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentErrorResultBinding.bind(view)

        binding.title.text = data.title
        binding.description.setHtmlText(data.description, htmlLinksEnabled = true)

        data.urlData?.let { urlData ->
            binding.urlButton.visibility = View.VISIBLE
            binding.urlButton.text = urlData.urlButtonTitle
            binding.urlButton.setOnClickListener {
                intentUtil.openUrl(requireActivity(), urlData.urlButtonUrl)
            }
        }

        binding.bottom.setButtonClick {
            when (val buttonAction = data.buttonAction) {
                is ErrorResultFragmentData.ButtonAction.Destination -> {
                    findNavControllerSafety()?.navigate(buttonAction.buttonDestinationId)
                }
                is ErrorResultFragmentData.ButtonAction.PopBackStack -> {
                    findNavControllerSafety()?.popBackStack()
                }
            }
        }
        binding.bottom.setButtonText(data.buttonTitle)
    }
}
