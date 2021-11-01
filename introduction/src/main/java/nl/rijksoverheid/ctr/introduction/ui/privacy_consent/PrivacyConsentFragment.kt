package nl.rijksoverheid.ctr.introduction.ui.privacy_consent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentPrivacyConsentBinding
import nl.rijksoverheid.ctr.introduction.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PrivacyConsentFragment : Fragment(R.layout.fragment_privacy_consent) {

    private val args: PrivacyConsentFragmentArgs by navArgs()
    private val introductionViewModel: IntroductionViewModel by viewModel()
    private lateinit var binding: FragmentPrivacyConsentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyConsentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPrivacyConsentBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            val canPop = findNavController().popBackStack()
            if (!canPop) {
                requireActivity().finish()
            }
        }

        args.introductionData.privacyPolicyItems.forEach { item ->
            val viewBinding =
                ItemPrivacyConsentBinding.inflate(layoutInflater, binding.items, true)
            viewBinding.icon.setImageResource(item.iconResource)
            viewBinding.description.setHtmlText(item.textResource,htmlLinksEnabled = false)
        }

        if (args.introductionData.hideConsent) {
            binding.checkboxContainer.visibility = View.GONE
            binding.bottom.setButtonEnabled(true)
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            binding.bottom.setButtonEnabled(isChecked)
        }

        binding.bottom.setButtonClick {
            introductionViewModel.saveIntroductionFinished(args.introductionData)
            requireActivity().findNavControllerSafety(R.id.main_nav_host_fragment)
                ?.navigate(R.id.action_main)
        }

        if (Accessibility.touchExploration(context)) {
            binding.toolbar.setAccessibilityFocus()
        }
    }
}
