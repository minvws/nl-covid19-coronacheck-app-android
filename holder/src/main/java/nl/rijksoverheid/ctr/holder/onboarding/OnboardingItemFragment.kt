package nl.rijksoverheid.ctr.holder.onboarding

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.HideToolbar
import nl.rijksoverheid.ctr.holder.databinding.FragmentOnboardingItemBinding
import nl.rijksoverheid.ctr.holder.onboarding.models.OnboardingItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingItemFragment : Fragment(), HideToolbar {

    companion object {

        private const val EXTRA_ONBOARDING_ITEM = "EXTRA_ONBOARDING_ITEM"

        fun getInstance(onboardingItem: OnboardingItem): OnboardingItemFragment {
            val fragment = OnboardingItemFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ONBOARDING_ITEM, onboardingItem)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var binding: FragmentOnboardingItemBinding
    private val item: OnboardingItem by lazy {
        arguments?.getParcelable<OnboardingItem>(
            EXTRA_ONBOARDING_ITEM
        ) ?: throw Exception("Failed to get item")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingItemBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = getString(item.titleResource)
        binding.description.text = Html.fromHtml(getString(item.descriptionResource))
    }
}
