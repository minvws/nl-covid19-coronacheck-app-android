package nl.rijksoverheid.ctr.introduction.ui.privacy_consent

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentPrivacyConsentBinding
import nl.rijksoverheid.ctr.introduction.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.ctr.introduction.databinding.WidgetScrollViewCheckboxButtonBinding
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

        val checkboxButtonBinding = WidgetScrollViewCheckboxButtonBinding.bind(binding.root)

        if (args.introductionData.hideConsent) {
            checkboxButtonBinding.checkboxContainer.visibility = View.GONE
        }

        checkboxButtonBinding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                resetErrorState(checkboxButtonBinding)
            }
        }

        checkboxButtonBinding.button.setOnClickListener {
            if (args.introductionData.hideConsent || checkboxButtonBinding.checkbox.isChecked) {
                introductionViewModel.saveIntroductionFinished(args.introductionData)
                requireActivity().findNavControllerSafety(R.id.main_nav_host_fragment)
                    ?.navigate(R.id.action_main)
            } else {
                showError(checkboxButtonBinding)
            }
        }

        if (Accessibility.touchExploration(context)) {
            binding.toolbar.setAccessibilityFocus()
        }
    }

    private fun showError(binding: WidgetScrollViewCheckboxButtonBinding) {
        binding.checkboxContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_privacy_policy_checkbox_background_error)
        binding.errorContainer.visibility = View.VISIBLE
        binding.checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.error))
        Accessibility.announce(context, getString(R.string.privacy_policy_checkbox_error))
    }

    private fun resetErrorState(binding: WidgetScrollViewCheckboxButtonBinding) {
        binding.checkboxContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_privacy_policy_checkbox_background)
        binding.errorContainer.visibility = View.GONE
        binding.checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_blue))
    }
}
