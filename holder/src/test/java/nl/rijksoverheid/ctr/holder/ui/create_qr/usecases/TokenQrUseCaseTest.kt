/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.TokenQR
import nl.rijksoverheid.ctr.shared.ext.toObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TokenQrUseCaseTest : AutoCloseKoinTest() {

    private val validInput =
        "{\"token\":\"TTTTTTTTT2\",\"protocolVersion\":\"1.0\",\"providerIdentifier\":\"BRB\"}"
    private val invalidInputMissingToken =
        "{\"protocolVersion\":\"1\",\"providerIdentifier\":\"BRB\"}"
    private val invalidInputIncorrectValues =
        "{\"token\":\"TTTTTTTTT2\",\"protocolVersion\":1,\"providerIdentifier\": BRB }"

    private val moshi: Moshi by inject()
    private val tokenQrUseCase = TokenQrUseCase(moshi)

    @Test
    fun `tokenQrResult returns Success if supplied with a valid token and providerIdentifier`() {
        val result = tokenQrUseCase.checkValidQR(validInput)
        assertTrue(result is TokenQrUseCase.TokenQrResult.Success)
    }

    @Test
    fun `tokenQrResult returns Failed if supplied with an invalid token or providerIdentifier`() {
        val result = tokenQrUseCase.checkValidQR(invalidInputMissingToken)
        assertTrue(result is TokenQrUseCase.TokenQrResult.Failed)
    }

    @Test
    fun `tokenQrResult returns Failed if supplied with malformed input`() {
        val result = tokenQrUseCase.checkValidQR(invalidInputIncorrectValues)
        assertTrue(result is TokenQrUseCase.TokenQrResult.Failed)
    }

    @Test
    fun `TokenQR with supplied token and providerIdentifier returns correct uniqueCode`() {
        val tokenQr = validInput.toObject<TokenQR>(moshi)
        val expectedResult = "${tokenQr.providerIdentifier}-${tokenQr.token}"
        val result = tokenQrUseCase.checkValidQR(validInput)
        assertEquals(
            expectedResult,
            (result as TokenQrUseCase.TokenQrResult.Success).uniqueCode
        )
    }
}
