package nl.rijksoverheid.ctr.verifier.introduction.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.introduction.onboarding.models.OnboardingItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        val item = when (position) {
            0 -> OnboardingItem(
                R.drawable.illustration_onboarding_1,
                R.string.onboarding_screen_1_title,
                R.string.onboarding_screen_1_description
            )
            1 -> OnboardingItem(
                R.drawable.illustration_onboarding_2,
                R.string.onboarding_screen_2_title,
                R.string.onboarding_screen_2_description
            )
            2 -> OnboardingItem(
                R.drawable.illustration_onboarding_3,
                R.string.onboarding_screen_3_title,
                R.string.onboarding_screen_3_description
            )
            3 -> OnboardingItem(
                R.drawable.illustration_onboarding_4,
                R.string.onboarding_screen_4_title,
                R.string.onboarding_screen_4_description
            )
            else -> throw Exception("Cannot create OnboardingItem for position")
        }
        return OnboardingItemFragment.getInstance(item)
    }

}
