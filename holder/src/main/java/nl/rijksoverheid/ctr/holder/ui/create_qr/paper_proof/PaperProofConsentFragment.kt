package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofConsentBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaperProofConsentFragment: Fragment(R.layout.fragment_paper_proof_consent) {

    private val args: PaperProofConsentFragmentArgs by navArgs()
    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperProofConsentBinding.bind(view)
        binding.bottom.setButtonClick {
            Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                .navigate(R.id.action_paper_proof_qr_scanner, bundleOf(PaperProofQrScannerFragment.EXTRA_COUPLING_CODE to args.couplingCode))
        }

        holderMainActivityViewModel.eventsLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(PaperProofConsentFragmentDirections.actionYourEvents(
                toolbarTitle = "",
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = mapOf(),
                    originType = OriginType.Vaccination
                )
            ))
        })
    }
}