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

        binding.appVersionMessage.text = helpdeskData.appVersion
        binding.configurationMessage.text = helpdeskData.configuration
    }
}
