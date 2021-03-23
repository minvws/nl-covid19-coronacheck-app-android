package nl.rijksoverheid.ctr.verifier.ui.scanqr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.fakeTestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.fakeVerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
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

    private val fakePersistenceManager: PersistenceManager = mockk(relaxed = true)
    private val loadingMockedObserver: Observer<Event<Boolean>> = mockk(relaxed = true)
    private val validatedQrObserver: Observer<Event<VerifiedQrResultState>> = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `Getting valid test result delegates to correct livedatas`() = runBlocking {
        val viewModel = ScanQrViewModelImpl(
            testResultValidUseCase = fakeTestResultValidUseCase(
                result = VerifiedQrResultState.Valid(
                    verifiedQr = fakeVerifiedQr()
                )
            ),
            persistenceManager = fakePersistenceManager
        )

        viewModel.loadingLiveData.observeForever(loadingMockedObserver)
        viewModel.validatedQrLiveData.observeForever(validatedQrObserver)

        viewModel.validate("")

        verifyOrder {
            loadingMockedObserver.onChanged(Event(true))
            loadingMockedObserver.onChanged(Event(false))
        }

        verify {
            validatedQrObserver.onChanged(
                Event(
                    VerifiedQrResultState.Valid(
                        verifiedQr = fakeVerifiedQr()
                    )
                )
            )
        }
    }

    @Test
    fun `Getting invalid test result delegates to correct livedatas`() = runBlocking {
        val viewModel = ScanQrViewModelImpl(
            testResultValidUseCase = fakeTestResultValidUseCase(
                result = VerifiedQrResultState.Invalid(
                    verifiedQr = null
                )
            ),
            persistenceManager = fakePersistenceManager
        )

        viewModel.loadingLiveData.observeForever(loadingMockedObserver)
        viewModel.validatedQrLiveData.observeForever(validatedQrObserver)

        viewModel.validate("")

        verifyOrder {
            loadingMockedObserver.onChanged(Event(true))
            loadingMockedObserver.onChanged(Event(false))
        }

        verify {
            validatedQrObserver.onChanged(
                Event(
                    VerifiedQrResultState.Invalid(
                        verifiedQr = null
                    )
                )
            )
        }
    }

    @Test
    fun `scanInstructionsSeen persist value if not persisted before`() {
        val viewModel = ScanQrViewModelImpl(
            testResultValidUseCase = fakeTestResultValidUseCase(
                result = VerifiedQrResultState.Invalid(
                    verifiedQr = null
                )
            ),
            persistenceManager = fakePersistenceManager
        )

        every { fakePersistenceManager.getScanInstructionsSeen() } answers { false }
        viewModel.scanInstructionsSeen()

        verify { fakePersistenceManager.setScanInstructionsSeen() }
    }

    @Test
    fun `scanInstructionsSeen does not persist value if persisted before`() {
        val viewModel = ScanQrViewModelImpl(
            testResultValidUseCase = fakeTestResultValidUseCase(
                result = VerifiedQrResultState.Invalid(
                    verifiedQr = null
                )
            ),
            persistenceManager = fakePersistenceManager
        )

        every { fakePersistenceManager.getScanInstructionsSeen() } answers { true }
        viewModel.scanInstructionsSeen()

        verify(exactly = 0) { fakePersistenceManager.setScanInstructionsSeen() }
    }
}
