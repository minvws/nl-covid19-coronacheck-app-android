package nl.rijksoverheid.ctr.verifier.ui.scanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.fakeTestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.fakeVerifiedQr
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState
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
class ScannerViewModelImplTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val loadingMockedObserver: Observer<Event<Boolean>> = mockk(relaxed = true)
    private val validatedQrObserver: Observer<Event<Pair<VerifiedQrResultState, ExternalReturnAppData?>>> =
        mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `Validating test result delegates to correct livedatas`() = runBlocking {
        val viewModel =
            ScannerViewModelImpl(testResultValidUseCase = fakeTestResultValidUseCase(), mockk(relaxed = true))

        viewModel.loadingLiveData.observeForever(loadingMockedObserver)
        viewModel.qrResultLiveData.observeForever(validatedQrObserver)

        viewModel.validate("", null)

        verifyOrder {
            loadingMockedObserver.onChanged(Event(true))
            loadingMockedObserver.onChanged(Event(false))
        }

        verify {
            validatedQrObserver.onChanged(
                Event(
                    VerifiedQrResultState.Valid(verifiedQr = fakeVerifiedQr()) to null
                )
            )
        }
    }
}
