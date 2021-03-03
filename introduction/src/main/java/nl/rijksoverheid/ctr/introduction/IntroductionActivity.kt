package nl.rijksoverheid.ctr.introduction

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.introduction.databinding.ActivityIntroductionBinding
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_policy.models.PrivacyPolicyItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionActivity : AppCompatActivity() {

    private val coronaCheckApp by lazy { application as CoronaCheckApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun getOnboardingItems(): List<OnboardingItem> {
        return coronaCheckApp.getIntroductionData().onboardingItems
    }

    fun getPrivacyPolicyItems(): List<PrivacyPolicyItem> {
        return coronaCheckApp.getIntroductionData().privacyPolicyItems
    }

    fun introductionDoneCallback(): (activity: Activity) -> Unit {
        return coronaCheckApp.getIntroductionData().introductionDoneCallback
    }

    fun getPrivacyPolicyDescription() : Int {
        return coronaCheckApp.getIntroductionData().privacyPolicyStringResource
    }

    fun getPrivacyPolicyCheckboxDescription() : Int {
        return coronaCheckApp.getIntroductionData().privacyPolicyCheckboxStringResource
    }

    fun getOnboardingNextString() : Int {
        return coronaCheckApp.getIntroductionData().onboardingNextButtonStringResource
    }

}
