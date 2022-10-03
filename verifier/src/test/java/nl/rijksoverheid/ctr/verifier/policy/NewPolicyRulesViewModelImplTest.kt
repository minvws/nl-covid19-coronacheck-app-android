/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.scanqr.ScannerNavigationState
import nl.rijksoverheid.ctr.verifier.scanqr.ScannerNavigationStateUseCase
import org.junit.Rule
import org.junit.Test

class NewPolicyRulesViewModelImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val persistenceManager: PersistenceManager = mockk(relaxed = true)
    private val newPolicyRulesItemUseCase: NewPolicyRulesItemUseCase = mockk()
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase = mockk()

    private val viewModel = NewPolicyRulesViewModelImpl(
        persistenceManager,
        newPolicyRulesItemUseCase, scannerNavigationStateUseCase
    )

    @Test
    fun `on next screen set new policy as seen`() {
        val verificationPolicySelection = ScannerNavigationState.VerificationPolicySelection
        every { scannerNavigationStateUseCase.get() } returns verificationPolicySelection

        viewModel.nextScreen()

        verify { persistenceManager.setNewPolicyRulesSeen(true) }
    }

    @Test
    fun `on init give the policy rule item`() {
        val newPolicyItem = NewPolicyItem(1, 1)
        every { newPolicyRulesItemUseCase.get() } returns newPolicyItem

        viewModel.init()

        assertEquals(newPolicyItem, viewModel.newPolicyRules.value)
    }

    @Test
    fun `on next screen give the scanner navigation state`() {
        val verificationPolicySelection = ScannerNavigationState.VerificationPolicySelection
        every { scannerNavigationStateUseCase.get() } returns verificationPolicySelection

        viewModel.nextScreen()

        assertEquals(verificationPolicySelection, viewModel.scannerNavigationStateEvent.value!!.peekContent())
    }
}
