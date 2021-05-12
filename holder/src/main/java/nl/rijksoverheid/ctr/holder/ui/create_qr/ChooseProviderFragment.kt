package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.databinding.IncludeTestProviderBinding
import nl.rijksoverheid.ctr.shared.ext.setAsAccessibilityButton
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChooseProviderFragment : Fragment(R.layout.fragment_choose_provider) {

    private val androidUtil: AndroidUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChooseProviderBinding.bind(view)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        }

        binding.providerCommercial.bind(
            R.string.choose_provider_commercial_title,
            null
        ) {
            findNavController().navigate(ChooseProviderFragmentDirections.actionCommercialTestCode())
        }

        binding.providerGgd.bind(
            R.string.choose_provider_ggd_title,
            getString(R.string.choose_provider_ggd_subtitle)
        ) {
        }

        binding.providerVaccination.bind(
            R.string.dummy_vaccination,
            null
        ) {
            findNavController().navigate(ChooseProviderFragmentDirections.actionVaccination())
        }

        binding.providerCommercial.root.setAsAccessibilityButton(isButton = true)
    }
}

private fun IncludeTestProviderBinding.bind(
    @StringRes title: Int,
    subtitle: String?,
    onClick: () -> Unit
) {
    providerTitle.setText(title)
    providerSubtitle.text = subtitle

    if (subtitle.isNullOrEmpty()) {
        providerSubtitle.visibility = View.GONE
        providerTitle.setPadding(
            providerTitle.paddingLeft,
            providerTitle.context.resources.getDimensionPixelSize(R.dimen.test_provider_title_without_subtitle_padding),
            providerTitle.paddingRight,
            providerTitle.context.resources.getDimensionPixelSize(R.dimen.test_provider_title_without_subtitle_padding)
        )
    }

    root.setOnClickListener {
        onClick()
    }
}
