/*
 *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecases

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase

class HolderIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData
) : IntroductionStatusUseCase {

    override fun getIntroductionRequired() =
        !introductionPersistenceManager.getIntroductionFinished()

    /**
     * Add the current disclosure policy info as onboarding item
     *
     * @return Onboarding not finished state with disclosure policy onboarding item added
     */
    override fun getData(): IntroductionData {
        return introductionData.copy(
            onboardingItems = getOnboardingItems()
        )
    }

    private fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem(
                R.drawable.illustration_onboarding_1_0g,
                R.string.holder_onboarding_content_TravelSafe_0G_title,
                R.string.holder_onboarding_content_TravelSafe_0G_message
            ),
            OnboardingItem(
                R.drawable.illustration_onboarding_2,
                R.string.onboarding_screen_2_title,
                R.string.onboarding_screen_2_description
            ),
            OnboardingItem(
                R.drawable.illustration_onboarding_3,
                R.string.holder_onboarding_content_onlyInternationalQR_0G_title,
                R.string.holder_onboarding_content_onlyInternationalQR_0G_message
            )
        )
    }
}
