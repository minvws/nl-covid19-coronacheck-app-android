/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.usecases

import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecases.HolderIntroductionStatusUseCaseImpl
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Test
import kotlin.test.assertTrue

class HolderIntroductionStatusUseCaseImplTest {

    private val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
    private val introductionData: IntroductionData = getIntroductionData()
    private val persistenceManager: PersistenceManager = mockk()
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
    private val introductionStatusUseCase = HolderIntroductionStatusUseCaseImpl(
        introductionPersistenceManager, introductionData, persistenceManager, holderFeatureFlagUseCase
    )

    @Test
    fun `when setup isn't finished, the status is setup not finished`() {
        every { introductionPersistenceManager.getSetupFinished() } returns false

        TestCase.assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.SetupNotFinished
        )
    }

    @Test
    fun `when introduction isn't finished, the status is introduction not finished with `() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertTrue(
            introductionStatusUseCase.get() is IntroductionStatus.OnboardingNotFinished
        )
    }

    @Test
    fun `when intro is finished and there are no new features or terms, the status is no action required`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.IntroductionFinished
        )
    }

    @Test
    fun `when feature flag is 1G, there should be a 1G onboarding item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only1GAccess_title,
                introductionData.onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only1GAccess_message,
                introductionData.onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                introductionData.onboardingItems.last().imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 3G, there should be a 3G onboarding item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only3GAccess_title,
                introductionData.onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only3GAccess_message,
                introductionData.onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                introductionData.onboardingItems.last().imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, the third onboarding item should have 0G text`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.holder_onboarding_content_onlyInternationalQR_0G_title,
                introductionData.onboardingItems[2].titleResource
            )
            assertEquals(
                R.string.holder_onboarding_content_onlyInternationalQR_0G_message,
                introductionData.onboardingItems[2].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_3,
                introductionData.onboardingItems[2].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is not 0G, the third onboarding item should have generic text`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.onboarding_screen_4_title,
                introductionData.onboardingItems[2].titleResource
            )
            assertEquals(
                R.string.onboarding_screen_4_description,
                introductionData.onboardingItems[2].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_3,
                introductionData.onboardingItems[2].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, the first onboarding item should have 0G text`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.holder_onboarding_content_TravelSafe_0G_title,
                introductionData.onboardingItems[0].titleResource
            )
            assertEquals(
                R.string.holder_onboarding_content_TravelSafe_0G_message,
                introductionData.onboardingItems[0].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_1_0g,
                introductionData.onboardingItems[0].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is not 0G, the fourth onboarding item should have generic text`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.onboarding_screen_3_title,
                introductionData.onboardingItems[3].titleResource
            )
            assertEquals(
                R.string.onboarding_screen_3_description,
                introductionData.onboardingItems[3].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_4,
                introductionData.onboardingItems[3].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, there is no policy onboarding and national screen`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(3, introductionData.onboardingItems.size)
        }
    }

    @Test
    fun `when feature flag is 1G+3G, there should be a 1G+3G onboarding item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneAndThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingNotFinished) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_3Gand1GAccess_title,
                introductionData.onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_3Gand1GAccess_message,
                introductionData.onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                introductionData.onboardingItems.last().imageResource
            )
        }
    }


    private fun getIntroductionData() = IntroductionData(
        onboardingItems = listOf(
            OnboardingItem(1, 2, 3)
        ),
        privacyPolicyItems = listOf(
            PrivacyPolicyItem(1, 2)
        )
    )
}
