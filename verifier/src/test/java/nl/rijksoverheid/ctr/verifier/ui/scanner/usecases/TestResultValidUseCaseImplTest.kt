package nl.rijksoverheid.ctr.verifier.ui.scanner.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.verifier.*
import nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers.VerifiedQrDataMapper
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultValidUseCaseImplTest {

    @Test
    fun `Validate returns Valid if code can be validated with valid test result and valid qr code`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(),
                testResultUtil = fakeTestResultUtil(),
                qrCodeUtil = fakeQrCodeUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase()
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Valid)
        }

    @Test
    fun `Validate returns Demo if code can be validated with valid test result, valid qr code and isSpecimen is set to 1`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(
                    result = VerifyQrUseCase.VerifyQrResult.Success(
                        verifiedQr = fakeVerifiedQr(isSpecimen = "1")
                    )
                ),
                testResultUtil = fakeTestResultUtil(),
                qrCodeUtil = fakeQrCodeUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase()
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Demo)
        }

    @Test
    fun `Validate returns Invalid if code can be validated with invalid test result and valid qr code`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(),
                testResultUtil = fakeTestResultUtil(isValid = false),
                qrCodeUtil = fakeQrCodeUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase()
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Invalid)
        }

    @Test
    fun `Validate returns Invalid if code can be validated with valid test result and invalid qr code`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(),
                testResultUtil = fakeTestResultUtil(),
                qrCodeUtil = fakeQrCodeUtil(isValid = false),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase()
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Invalid)
        }

    @Test
    fun `Validate returns Error if code cannot be validated`() = runBlocking {
        val fakeVerifiedQrDataMapper: VerifiedQrDataMapper = mockk()
        coEvery { fakeVerifiedQrDataMapper.transform(any()) } throws Exception("Crash")
        val fakeVerifyQrUseCase = VerifyQrUseCaseImpl(fakeVerifiedQrDataMapper)

        val usecase = TestResultValidUseCaseImpl(
            verifyQrUseCase = fakeVerifyQrUseCase,
            testResultUtil = fakeTestResultUtil(),
            qrCodeUtil = fakeQrCodeUtil(isValid = false),
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase()
        )
        assertTrue(usecase.validate("") is VerifiedQrResultState.Error)
    }
}
