package nl.rijksoverheid.ctr.introduction.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingPagerAdapter(fragment: Fragment, private val items: List<OnboardingItem>) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return OnboardingItemFragment.getInstance(item)
    }

}
