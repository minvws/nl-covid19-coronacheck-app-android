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
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ShowNewDisclosurePolicyUseCase
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
    fun `when introduction isn't finished, the status is introduction not finished with `() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG
        every { showNewDisclosurePolicyUseCase.get() } returns DisclosurePolicy.OneG

        assertTrue(
            introductionStatusUseCase.get() is IntroductionStatus.OnboardingNotFinished
        )
    }

    @Test
    fun `when intro is finished and new features are available, the status is new features`() {
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
