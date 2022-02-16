/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNewDisclosurePolicyBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewDisclosurePolicyFragment : Fragment(R.layout.fragment_new_disclosure_policy) {

    private val newDisclosurePolicyViewModel: NewDisclosurePolicyViewModel by viewModel()
    private val androidUtil: AndroidUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNewDisclosurePolicyBinding.bind(view)
        bindButton(binding)
        handleSmallScreen(binding)
        setObserver(binding)

        newDisclosurePolicyViewModel.init()
    }

    private fun handleSmallScreen(binding: FragmentNewDisclosurePolicyBinding) {
        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
        }
    }

    private fun setObserver(binding: FragmentNewDisclosurePolicyBinding) {
        newDisclosurePolicyViewModel.disclosurePolicyLiveData.observe(viewLifecycleOwner) {
            setPolicyText(it, binding)
        }
    }

    private fun bindButton(binding: FragmentNewDisclosurePolicyBinding) {
        binding.bottomButtonBar.setButtonClick {
            findNavControllerSafety()?.popBackStack()
        }
    }

    private fun setPolicyText(
        policy: DisclosurePolicy,
        binding: FragmentNewDisclosurePolicyBinding
    ) {
        when (policy) {
            DisclosurePolicy.OneG -> {
                binding.title.text = getString(R.string.holder_newintheapp_content_only1G_title)
                binding.description.text =
                    getString(R.string.holder_newintheapp_content_only1G_body)
            }
            DisclosurePolicy.ThreeG -> {
                binding.title.text = getString(R.string.holder_newintheapp_content_only3G_title)
                binding.description.text =
                    getString(R.string.holder_newintheapp_content_only3G_body)
            }
            DisclosurePolicy.OneAndThreeG -> {
                binding.title.text = getString(R.string.holder_newintheapp_content_3Gand1G_title)
                binding.description.text =
                    getString(R.string.holder_newintheapp_content_3Gand1G_body)
            }
        }
    }
}