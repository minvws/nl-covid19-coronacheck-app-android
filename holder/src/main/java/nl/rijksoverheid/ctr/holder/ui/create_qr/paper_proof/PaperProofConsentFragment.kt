package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofConsentBinding

class PaperProofConsentFragment: Fragment(R.layout.fragment_paper_proof_consent) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperProofConsentBinding.bind(view)
        binding.bottom.setOnClickListener {

        }
    }
}