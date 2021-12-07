package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrViewModelImplTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val persistenceManager: PersistenceManager = mockk(relaxed = true)
    private val verificationPolicyUseCase: VerificationPolicyUseCase = mockk(relaxed = true)
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `scanInstructionsSeen persist value if not persisted before`() {
        val viewModel = ScanQrViewModelImpl(persistenceManager, verificationPolicyUseCase, scannerNavigationStateUseCase)

        every { persistenceManager.getScanInstructionsSeen() } answers { false }
        viewModel.setScanInstructionsSeen()

        verify { persistenceManager.setScanInstructionsSeen() }
    }

    @Test
    fun `scanInstructionsSeen does not persist value if persisted before`() {
        val viewModel = ScanQrViewModelImpl(persistenceManager, verificationPolicyUseCase, scannerNavigationStateUseCase)

        every { persistenceManager.getScanInstructionsSeen() } answers { true }
        viewModel.setScanInstructionsSeen()

        verify(exactly = 0) { persistenceManager.setScanInstructionsSeen() }
    }
}
