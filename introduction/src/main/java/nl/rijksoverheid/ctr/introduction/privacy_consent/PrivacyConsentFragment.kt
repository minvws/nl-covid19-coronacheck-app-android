package nl.rijksoverheid.ctr.introduction.privacy_consent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.introduction.BuildConfig
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentPrivacyConsentBinding
import nl.rijksoverheid.ctr.introduction.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PrivacyConsentFragment : Fragment(R.layout.fragment_privacy_consent) {

    private val onboardingData by lazy { (requireActivity().application as CoronaCheckApp).getOnboardingData() }
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
        binding.toolbar.navigationContentDescription =
            getString(onboardingData.backButtonStringResource)

        binding.description.text =
            getString(onboardingData.privacyPolicyStringResource).fromHtml()
        binding.description.setOnClickListener {
            BuildConfig.URL_PRIVACY_STATEMENT.launchUrl(requireContext())
        }
        binding.checkbox.text =
            getString(onboardingData.privacyPolicyCheckboxStringResource)
        binding.button.text = getString(onboardingData.onboardingNextButtonStringResource)

        onboardingData.privacyPolicyItems.forEach { item ->
            val viewBinding =
                ItemPrivacyConsentBinding.inflate(layoutInflater, binding.items, true)
            viewBinding.icon.setImageResource(item.iconResource)
            viewBinding.description.text =
                viewBinding.description.context.getString(item.textResource).fromHtml()
        }
        Log.d("bart", "check: " + binding.scroll.canScrollVertically(1))
        binding.scroll.doOnPreDraw {
            if (binding.scroll.canScrollVertically(1)) {
                binding.bottom.cardElevation =
                    resources.getDimensionPixelSize(R.dimen.onboarding_bottom_scroll_elevation)
                        .toFloat()
            } else {
                binding.bottom.cardElevation = 0f
            }
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            binding.button.isEnabled = isChecked
        }

        binding.button.setOnClickListener {
            introductionViewModel.saveIntroductionFinished()
            //findNavController().navigate(PrivacyConsentFragmentDirections.navExit())
            //onboardingData.introductionDoneCallback.invoke(this)
        }
    }
}
