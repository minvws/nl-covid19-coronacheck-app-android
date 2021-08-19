package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.databinding.FragmentCouldNotCreateQrBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CouldNotCreateQrFragment : Fragment() {

    private val args: CouldNotCreateQrFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCouldNotCreateQrBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCouldNotCreateQrBinding.bind(view)
        binding.title.text = args.title
        binding.description.setHtmlText(args.description, htmlLinksEnabled = true)
        binding.bottom.setButtonClick {
            findNavController().navigate(CouldNotCreateQrFragmentDirections.actionMyOverview())
        }
        binding.bottom.setButtonText(args.buttonTitle)
    }
}
