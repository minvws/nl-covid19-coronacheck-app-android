package nl.rijksoverheid.ctr.introduction.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem

abstract class OnboardingBaseItemFragment(layoutResId: Int) : Fragment(layoutResId) {
    companion object {
        const val EXTRA_ONBOARDING_ITEM = "EXTRA_ONBOARDING_ITEM"

        fun getInstance(onboardingItem: OnboardingItem): OnboardingBaseItemFragment {
            val fragment =
                onboardingItem.clazz.newInstance()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ONBOARDING_ITEM, onboardingItem)
            fragment.arguments = bundle
            return fragment
        }
    }
}
