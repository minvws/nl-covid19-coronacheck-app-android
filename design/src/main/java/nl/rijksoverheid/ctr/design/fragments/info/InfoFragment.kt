package nl.rijksoverheid.ctr.design.fragments.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentInfoBinding
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import org.koin.android.ext.android.inject

class InfoFragment : Fragment(R.layout.fragment_info) {

    private val intentUtil: IntentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentInfoBinding.bind(view)

        val infoFragmentData = arguments?.get(InfoFragmentUtil.EXTRA_INFO_FRAGMENT_DATA) as? InfoFragmentData ?: return
        binding.title.text = infoFragmentData.title
        binding.description.apply {
            infoFragmentData.descriptionData.run {
                htmlText?.let {
                    setHtmlText(it, htmlLinksEnabled) }
                htmlTextString?.let {
                    setHtmlText(it, htmlLinksEnabled) }
                customLinkIntent?.let { enableCustomLinks { context.startActivity(it) } }
            }
        }
        when (infoFragmentData) {
            is InfoFragmentData.TitleDescription -> {}
            is InfoFragmentData.TitleDescriptionWithButton -> {

                infoFragmentData.secondaryButtonData?.let { buttonData ->
                    binding.button.visibility = View.VISIBLE
                    binding.button.apply {
                        text = buttonData.text
                        setOnClickListener {
                            when (buttonData) {
                                is ButtonData.NavigationButton -> {
                                    findNavControllerSafety()?.navigate(buttonData.navigationActionId, buttonData.navigationArguments)
                                }
                                is ButtonData.LinkButton -> {
                                    intentUtil.openUrl(
                                        context = requireContext(),
                                        url = buttonData.link
                                    )
                                }
                            }
                        }
                    }
                }

                infoFragmentData.primaryButtonData?.let { buttonData ->
                    binding.bottom.visibility = View.VISIBLE
                    binding.bottom.setButtonText(buttonData.text)
                    binding.bottom.setButtonClick {
                        when (buttonData) {
                            is ButtonData.NavigationButton -> {
                                findNavControllerSafety()?.navigate(buttonData.navigationActionId, buttonData.navigationArguments)
                            }
                            is ButtonData.LinkButton -> {
                                intentUtil.openUrl(
                                    context = requireContext(),
                                    url = buttonData.link
                                )
                            }
                        }
                    }
                }
            }
            is InfoFragmentData.TitleDescriptionWithFooter -> {
                binding.footer.text = infoFragmentData.footerText
            }
        }
    }
}