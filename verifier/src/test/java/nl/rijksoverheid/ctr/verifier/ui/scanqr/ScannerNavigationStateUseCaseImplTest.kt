/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.scanqr

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase
import org.junit.Test
import kotlin.test.assertEquals

class ScannerNavigationStateUseCaseImplTest {

    private val persistenceManager: PersistenceManager = mockk()
    private val scannerStateUseCase: ScannerStateUseCase = mockk()
    private val featureFlagUseCase: FeatureFlagUseCase = mockk()

    private val useCase = ScannerNavigationStateUseCaseImpl(
        persistenceManager, scannerStateUseCase, featureFlagUseCase
    )

    @Test
    fun `instructions state should be given when instructions are not seen`() {
        every { persistenceManager.getScanInstructionsSeen() } returns false

        assertEquals(ScannerNavigationState.Instructions, useCase.get())
    }

    @Test
    fun `new policy rules state should be given when new policy is not seen`() {
        every { persistenceManager.getScanInstructionsSeen() } returns true
        every { persistenceManager.getNewPolicyRulesSeen() } returns false

        assertEquals(ScannerNavigationState.NewPolicyRules, useCase.get())
    }

    @Test
    fun `selection state should be given when selection is not set and selection is enabled`() {
        every { persistenceManager.getScanInstructionsSeen() } returns true
        every { persistenceManager.getNewPolicyRulesSeen() } returns true
        every { persistenceManager.isVerificationPolicySelectionSet() } returns false
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true

        assertEquals(ScannerNavigationState.VerificationPolicySelection, useCase.get())
    }

    @Test
    fun `scanner locked state should be given when all has been shown and scanner is locked`() {
        every { persistenceManager.getScanInstructionsSeen() } returns true
        every { persistenceManager.getNewPolicyRulesSeen() } returns true
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns false
        every { scannerStateUseCase.get() } returns ScannerState.Locked(1, VerificationPolicySelectionState.Policy1G)

        assertEquals(ScannerNavigationState.Scanner(true), useCase.get())
    }

    @Test
    fun `scanner unlocked state should be given when all has been shown and scanner is unlocked`() {
        every { persistenceManager.getScanInstructionsSeen() } returns true
        every { persistenceManager.getNewPolicyRulesSeen() } returns true
        every { persistenceManager.isVerificationPolicySelectionSet() } returns true
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns false
        every { scannerStateUseCase.get() } returns ScannerState.Unlocked(VerificationPolicySelectionState.Policy1G)

        assertEquals(ScannerNavigationState.Scanner(false), useCase.get())
    }
}