package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentExpiredTestResultBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety

class ExpiredTestResultFragment : Fragment(R.layout.fragment_expired_test_result) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding  = FragmentExpiredTestResultBinding.bind(view)
        binding.bottom.setOnClickListener {
            findNavControllerSafety(R.id.nav_expired_test_result)?.navigate(
                ExpiredTestResultFragmentDirections.actionMyOverview()
            )
        }
    }
}