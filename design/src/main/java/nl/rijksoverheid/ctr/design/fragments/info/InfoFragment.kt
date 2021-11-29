package nl.rijksoverheid.ctr.design.fragments.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentInfoBinding
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl

class InfoFragment : Fragment(R.layout.fragment_info) {

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
                binding.button.visibility = View.VISIBLE
                binding.button.apply {
                    val buttonData = infoFragmentData.buttonData
                    if (buttonData is ButtonData.NavigationButton) {
                        text = buttonData.text
                        setOnClickListener { findNavControllerSafety()?.navigate(buttonData.navigationActionId) }
                    }
                }
            }
            is InfoFragmentData.TitleDescriptionWithFooter -> {
                binding.footer.text = infoFragmentData.footerText
            }
        }
    }
}