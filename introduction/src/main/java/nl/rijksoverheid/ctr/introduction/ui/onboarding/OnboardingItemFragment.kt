package nl.rijksoverheid.ctr.introduction.ui.onboarding

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentOnboardingItemBinding
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingItemFragment : Fragment(R.layout.fragment_onboarding_item) {

    companion object {
        private const val EXTRA_ONBOARDING_ITEM = "EXTRA_ONBOARDING_ITEM"

        fun getInstance(onboardingItem: OnboardingItem): OnboardingItemFragment {
            val fragment =
                OnboardingItemFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ONBOARDING_ITEM, onboardingItem)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val androidUtil: AndroidUtil by inject()

    private val item: OnboardingItem by lazy {
        arguments?.getParcelable<OnboardingItem>(
            EXTRA_ONBOARDING_ITEM
        ) ?: throw Exception("Failed to get item")
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingItemBinding.bind(view)

        binding.title.text = getString(item.titleResource)
        binding.description.setHtmlText(item.description, htmlLinksEnabled = false)

        // If a position is set (default = -1) show "Step x of x" above header
        if (item.position >= 0) {
            binding.step.apply {
                visibility = View.VISIBLE
                text = getString(R.string.onboarding_step, item.position)
            }
        }

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
            if (item.imageResource != 0) {
                binding.image.setImageResource(item.imageResource)
            } else if (item.animationResource != 0) {
                binding.image.setAnimation(item.animationResource)
                binding.image.playAnimation()
            }
        }
    }
}
