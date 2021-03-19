package nl.rijksoverheid.ctr.introduction.privacy_consent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.introduction.BuildConfig
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentPrivacyConsentBinding
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

        binding.description.text =
            getString(onboardingData.privacyPolicyStringResource).fromHtml()
        binding.description.setOnClickListener {
            BuildConfig.URL_PRIVACY_STATEMENT.launchUrl(requireContext())
        }
        binding.checkbox.text =
            getString(onboardingData.privacyPolicyCheckboxStringResource)
        binding.button.text = getString(onboardingData.onboardingNextButtonStringResource)

        val adapterItems = onboardingData.privacyPolicyItems.map {
            PrivacyConsentAdapterItem(
                it
            )
        }

        val adapter = GroupieAdapter()
        val section = Section()
        binding.items.adapter = adapter
        adapter.add(section)
        section.update(adapterItems)

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            binding.button.isEnabled = isChecked
        }

        binding.button.setOnClickListener {
            introductionViewModel.saveIntroductionFinished()
            onboardingData.introductionDoneCallback.invoke(this)
        }
    }
}
