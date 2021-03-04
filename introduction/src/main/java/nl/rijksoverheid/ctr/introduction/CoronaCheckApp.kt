package nl.rijksoverheid.ctr.introduction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_policy.models.PrivacyPolicyItem

interface CoronaCheckApp {
    fun getIntroductionData(): IntroductionData

    data class IntroductionData(
        val onboardingItems: List<OnboardingItem> = listOf(),
        val privacyPolicyItems: List<PrivacyPolicyItem> = listOf(),
        val introductionDoneCallback: (fragment: Fragment) -> Unit,
        @StringRes val appSetupTextResource: Int,
        @StringRes val privacyPolicyStringResource: Int,
        @StringRes val privacyPolicyCheckboxStringResource: Int,
        @StringRes val onboardingNextButtonStringResource: Int,
        @DrawableRes val launchScreen: Int
    )
}
