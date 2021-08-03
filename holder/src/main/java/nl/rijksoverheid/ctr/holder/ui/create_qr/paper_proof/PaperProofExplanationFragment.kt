package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofExplanationFragmentBinding
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

class PaperProofExplanationFragment: Fragment(R.layout.fragment_paper_proof_explanation_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperProofExplanationFragmentBinding.bind(view)
        binding.bottom.setButtonClick {
            navigateSafety(PaperProofExplanationFragmentDirections.actionPaperProofCode())
        }
    }
}