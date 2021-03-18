package nl.rijksoverheid.ctr.introduction

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem

interface CoronaCheckApp {
    fun getSetupData(): SetupData
    fun getOnboardingData(): OnboardingData

    data class SetupData(
        @StringRes val appSetupTextResource: Int
    )

    data class OnboardingData(
        val onboardingItems: List<OnboardingItem> = listOf(),
        val privacyPolicyItems: List<PrivacyPolicyItem> = listOf(),
        val introductionDoneCallback: (fragment: Fragment) -> Unit,
        @StringRes val privacyPolicyStringResource: Int,
        @StringRes val privacyPolicyCheckboxStringResource: Int,
        @StringRes val onboardingNextButtonStringResource: Int,
    )
}
