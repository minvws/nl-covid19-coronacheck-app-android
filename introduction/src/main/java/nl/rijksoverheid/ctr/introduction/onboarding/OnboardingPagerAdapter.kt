package nl.rijksoverheid.ctr.introduction.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val items: List<OnboardingItem>
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return getInstance(item)
    }

    companion object {
        const val EXTRA_ONBOARDING_ITEM = "EXTRA_ONBOARDING_ITEM"
        private fun getInstance(onboardingItem: OnboardingItem): Fragment {
            val fragment =
                onboardingItem.clazz.newInstance() as Fragment
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ONBOARDING_ITEM, onboardingItem)
            fragment.arguments = bundle
            return fragment
        }
    }
}
