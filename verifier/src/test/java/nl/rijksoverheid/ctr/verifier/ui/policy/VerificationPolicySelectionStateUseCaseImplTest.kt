/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.policy

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import org.junit.Test
import kotlin.test.assertEquals

class VerificationPolicySelectionStateUseCaseImplTest {

    private val persistenceManager: PersistenceManager = mockk()
    private val featureFlagUseCase: FeatureFlagUseCase = mockk()
    private val useCase =
        VerificationPolicySelectionStateUseCaseImpl(persistenceManager, featureFlagUseCase)

    @Test
    fun `when selection is disabled get 3G state from persistence manager`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns false
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy3G

        assertEquals(VerificationPolicySelectionState.Policy3G, useCase.get())
    }

    @Test
    fun `when selection is disabled get 1G state from persistence manager`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns false
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy1G

        assertEquals(VerificationPolicySelectionState.Policy1G, useCase.get())
    }

    @Test
    fun `when selection is enabled get 1G selected state from persistence manager`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy1G

        assertEquals(VerificationPolicySelectionState.Selection.Policy1G, useCase.get())
    }

    @Test
    fun `when selection is enabled get 3G selected state from persistence manager`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns VerificationPolicy.VerificationPolicy3G

        assertEquals(VerificationPolicySelectionState.Selection.Policy3G, useCase.get())
    }

    @Test
    fun `when selection is enabled get none selected state from persistence manager`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true
        every { persistenceManager.getVerificationPolicySelected() } returns null

        assertEquals(VerificationPolicySelectionState.Selection.None, useCase.get())
    }
}