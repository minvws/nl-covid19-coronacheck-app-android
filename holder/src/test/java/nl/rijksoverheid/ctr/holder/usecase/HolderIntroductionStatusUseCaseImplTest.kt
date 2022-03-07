/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.usecase

import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.new_features.models.NewFeatureItem
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.ui.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Test
import kotlin.test.assertTrue

class HolderIntroductionStatusUseCaseImplTest {

    private val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
    private val introductionData: IntroductionData = getIntroductionData()
    private val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase = mockk()
    private val persistenceManager: PersistenceManager = mockk()
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase = mockk()
    private val introductionStatusUseCase = HolderIntroductionStatusUseCaseImpl(
        introductionPersistenceManager, introductionData,
        showNewDisclosurePolicyUseCase, persistenceManager, holderFeatureFlagUseCase
    )

    @Test
    fun `when setup isn't finished, the status is setup not finished`() {
        every { introductionPersistenceManager.getSetupFinished() } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG

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
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG

        assertTrue(
            introductionStatusUseCase.get() is IntroductionStatus.OnboardingNotFinished
        )
    }

    @Test
    fun `when intro is finished and new features are available, the status is new features`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertTrue(
            introductionStatusUseCase.get() is IntroductionStatus.OnboardingFinished.NewFeatures
        )
    }

    @Test
    fun `when intro is finished and new terms are available, the status is consent needed`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns true
        every { introductionPersistenceManager.getNewTermsSeen(1) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns null
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        assertTrue(
            introductionStatusUseCase.get() is IntroductionStatus.OnboardingFinished.ConsentNeeded
        )
    }

    @Test
    fun `when intro is finished and there are no new features or terms, the status is no action required`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns true
        every { introductionPersistenceManager.getNewTermsSeen(1) } returns true
        every { showNewDisclosurePolicyUseCase.get() } returns null
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
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG

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
            assertEquals(2, introductionData.onboardingItems.last().position)
        }
    }

    @Test
    fun `when feature flag is 3G, there should be a 3G onboarding item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG

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
            assertEquals(2, introductionData.onboardingItems.last().position)
        }
    }

    @Test
    fun `when feature flag is 1G+3G, there should be a 1G+3G onboarding item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneAndThreeG
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneAndThreeG

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
            assertEquals(2, introductionData.onboardingItems.last().position)
        }
    }

    @Test
    fun `when disclosure is 1G+3G, there should be a 1G+3G new feature item added`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneAndThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneAndThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingFinished.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_3Gand1G_title,
                introductionData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_3Gand1G_body,
                introductionData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                introductionData.newFeatures.last().imageResource
            )
            assertEquals(2, introductionData.newFeatures.size)
        }
    }

    @Test
    fun `when disclosure is 1G, there should be a 1G new feature item added`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingFinished.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only1G_title,
                introductionData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only1G_body,
                introductionData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                introductionData.newFeatures.last().imageResource
            )
            assertEquals(2, introductionData.newFeatures.size)
        }
    }

    @Test
    fun `when disclosure is 3G, there should be a 3G new feature item added`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingFinished.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only3G_title,
                introductionData.newFeatures.last().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only3G_body,
                introductionData.newFeatures.last().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                introductionData.newFeatures.last().imageResource
            )
            assertEquals(2, introductionData.newFeatures.size)
        }
    }

    @Test
    fun `when there are no new feature but there is a policy change, there should be a new feature item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns true
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.ThreeG
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingFinished.NewFeatures) {
            assertEquals(
                R.string.holder_newintheapp_content_only3G_title,
                introductionData.newFeatures.first().titleResource
            )
            assertEquals(
                R.string.holder_newintheapp_content_only3G_body,
                introductionData.newFeatures.first().description
            )
            assertEquals(
                R.drawable.illustration_new_disclosure_policy,
                introductionData.newFeatures.first().imageResource
            )
            assertEquals(1, introductionData.newFeatures.size)
            assertEquals(null, introductionData.newFeatureVersion)
        }
    }

    @Test
    fun `when there is new feature but no policy change, there should not be a policy new feature item`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false
        every { showNewDisclosurePolicyUseCase.get() } returns null
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG

        val status = introductionStatusUseCase.get()

        with(status as IntroductionStatus.OnboardingFinished.NewFeatures) {
            assertEquals(1, introductionData.newFeatures.size)
            assertEquals(2, introductionData.newFeatureVersion)
        }
    }

    private fun getIntroductionData() = IntroductionData(
        onboardingItems = listOf(
            OnboardingItem(1, 2, 3)
        ),
        privacyPolicyItems = listOf(
            PrivacyPolicyItem(1, 2)
        ),
        newTerms = NewTerms(
            version = 1,
            needsConsent = false
        ),
        newFeatures = listOf(
            NewFeatureItem(1, 2, 3)
        ),
        newFeatureVersion = 2,
        hideConsent = true
    )
}
