/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.certificate_created

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCertificateCreatedBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety

class CertificateCreatedFragment :
    Fragment(R.layout.fragment_certificate_created) {

    private val args: CertificateCreatedFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCertificateCreatedBinding.bind(view)
        binding.bottom.setButtonClick { backToOverview() }
        with(args) {
            binding.title.text = title
            binding.description.setHtmlText(
                htmlText = description,
                htmlLinksEnabled = true
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToOverview()
            }
        })
    }

    private fun backToOverview() {
        findNavControllerSafety()?.popBackStack()
    }
}
