package nl.rijksoverheid.ctr.verifier.ui.scanner.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.verifier.*
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
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Demo)
        }

    @Test
    fun `Validate returns Invalid if code has isNLDCC 1`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(isNLDCC = true),
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.InvalidInNL)
        }

    @Test
    fun `Validate returns Demo if code has Specimen 1`() =
        runBlocking {
            val usecase = TestResultValidUseCaseImpl(
                verifyQrUseCase = fakeVerifyQrUseCase(isSpecimen = "1"),
            )
            assertTrue(usecase.validate("") is VerifiedQrResultState.Demo)
        }

    @Test
    fun `Validate returns Error if code cannot be validated`() = runBlocking {

        val usecase = TestResultValidUseCaseImpl(
            verifyQrUseCase = fakeVerifyQrUseCase(error = true),
        )
        assertTrue(usecase.validate("") is VerifiedQrResultState.Error)
    }
}
