package nl.rijksoverheid.ctr.design.menu.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentHelpdeskBinding
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat

class HelpdeskFragment : Fragment(R.layout.fragment_helpdesk) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHelpdeskBinding.bind(view)

        val helpdeskData = arguments?.getParcelableCompat<HelpdeskData>("data")
            ?: throw IllegalStateException("HelpdeskData should be set")

        binding.contactTitle.text = helpdeskData.contactTitle
        binding.contactMessageLine1.setHtmlText(helpdeskData.contactMessageLines[0], true)
        binding.contactMessageLine2.setHtmlText(helpdeskData.contactMessageLines[1], true)
        binding.contactMessageLine3.setHtmlText(helpdeskData.contactMessageLines[2], true)
        binding.supportTitle.text = helpdeskData.supportTitle
        binding.supportMessage.setHtmlText(helpdeskData.supportMessage, true)
        binding.appVersionTitle.text = helpdeskData.appVersionTitle
        binding.appVersionMessage.text = helpdeskData.appVersion
        binding.configurationTitle.text = helpdeskData.configurationTitle
        binding.configurationMessage.text = helpdeskData.configuration
    }
}
