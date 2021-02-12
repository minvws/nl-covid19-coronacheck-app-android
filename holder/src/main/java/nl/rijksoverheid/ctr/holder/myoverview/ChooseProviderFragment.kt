package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.databinding.IncludeTestProviderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChooseProviderFragment : Fragment() {

    private lateinit var binding: FragmentChooseProviderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.providerCommercial.bind(
            R.string.choose_provider_commercial_title,
            R.string.choose_provider_commercial_subtitle
        ) {
            findNavController().navigate(ChooseProviderFragmentDirections.actionCommercialTestType())
        }

        binding.providerGgd.bind(
            R.string.choose_provider_ggd_title,
            R.string.choose_provider_ggd_subtitle
        ) {
        }
    }
}

private fun IncludeTestProviderBinding.bind(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    onClick: () -> Unit
) {
    providerTitle.setText(title)
    providerSubtitle.setText(subtitle)
    root.setOnClickListener {
        onClick()
    }
}