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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecases.HolderIntroductionStatusUseCaseImpl
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Test

class HolderIntroductionStatusUseCaseImplTest {

    private val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
    private val introductionData: IntroductionData = getIntroductionData()
    private val persistenceManager: PersistenceManager = mockk()
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
    private val introductionStatusUseCase = HolderIntroductionStatusUseCaseImpl(
        introductionPersistenceManager, introductionData, persistenceManager, holderFeatureFlagUseCase
    )

    @Test
    fun `when introduction isn't finished, introduction is required`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertTrue(introductionStatusUseCase.getIntroductionRequired())
    }

    @Test
    fun `when intro is finished, introduction is not required`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertFalse(introductionStatusUseCase.getIntroductionRequired())
    }

    @Test
    fun `when feature flag is 1G, there should be a 1G onboarding item`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only1GAccess_title,
                onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only1GAccess_message,
                onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                onboardingItems.last().imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 3G, there should be a 3G onboarding item`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only3GAccess_title,
                onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_only3GAccess_message,
                onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                onboardingItems.last().imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, the third onboarding item should have 0G text`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.holder_onboarding_content_onlyInternationalQR_0G_title,
                onboardingItems[2].titleResource
            )
            assertEquals(
                R.string.holder_onboarding_content_onlyInternationalQR_0G_message,
                onboardingItems[2].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_3,
                onboardingItems[2].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is not 0G, the third onboarding item should have generic text`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.onboarding_screen_4_title,
                onboardingItems[2].titleResource
            )
            assertEquals(
                R.string.onboarding_screen_4_description,
                onboardingItems[2].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_3,
                onboardingItems[2].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, the first onboarding item should have 0G text`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.holder_onboarding_content_TravelSafe_0G_title,
                onboardingItems[0].titleResource
            )
            assertEquals(
                R.string.holder_onboarding_content_TravelSafe_0G_message,
                onboardingItems[0].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_1_0g,
                onboardingItems[0].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is not 0G, the fourth onboarding item should have generic text`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.onboarding_screen_3_title,
                onboardingItems[3].titleResource
            )
            assertEquals(
                R.string.onboarding_screen_3_description,
                onboardingItems[3].description
            )
            assertEquals(
                R.drawable.illustration_onboarding_4,
                onboardingItems[3].imageResource
            )
        }
    }

    @Test
    fun `when feature flag is 0G, there is no policy onboarding and national screen`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(3, onboardingItems.size)
        }
    }

    @Test
    fun `when feature flag is 1G+3G, there should be a 1G+3G onboarding item`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneAndThreeG

        val data = introductionStatusUseCase.getData()

        with(data) {
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_3Gand1GAccess_title,
                onboardingItems.last().titleResource
            )
            assertEquals(
                R.string.holder_onboarding_disclosurePolicyChanged_3Gand1GAccess_message,
                onboardingItems.last().description
            )
            assertEquals(
                R.drawable.illustration_onboarding_disclosure_policy,
                onboardingItems.last().imageResource
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
