/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecase

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.holder.myoverview.models.TokenQR
import nl.rijksoverheid.ctr.shared.ext.toObject
import org.junit.Assert
import org.junit.Test

class TokenQrUseCaseTest {

    private val validInput =
        "{\"token\":\"TTTTTTTTT2\",\"protocolVersion\":\"1.0\",\"providerIdentifier\":\"BRB\"}"
    private val invalidInputMissingToken =
        "{\"protocolVersion\":\"1\",\"providerIdentifier\":\"BRB\"}"
    private val invalidInputIncorrectValues =
        "{\"token\":\"TTTTTTTTT2\",\"protocolVersion\":1,\"providerIdentifier\": BRB }"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()).build()
    private val tokenQrUseCase = TokenQrUseCase(moshi)

    @Test
    fun `tokenQrResult returns Success if supplied with a valid token and providerIdentifier`() {
        val result = tokenQrUseCase.checkValidQR(validInput)

        Assert.assertTrue(result is TokenQrUseCase.TokenQrResult.Success)
    }

    @Test
    fun `tokenQrResult returns Failed if supplied with an invalid token or providerIdentifier`() {
        val result = tokenQrUseCase.checkValidQR(invalidInputMissingToken)

        Assert.assertTrue(result is TokenQrUseCase.TokenQrResult.Failed)
    }

    @Test
    fun `tokenQrResult returns Failed if supplied with malformed input`() {
        val result = tokenQrUseCase.checkValidQR(invalidInputIncorrectValues)

        Assert.assertTrue(result is TokenQrUseCase.TokenQrResult.Failed)
    }

    @Test
    fun `TokenQR with supplied token and providerIdentifier returns correct uniqueCode`() {
        val tokenQr = validInput.toObject<TokenQR>(moshi)
        val expectedResult = "${tokenQr.providerIdentifier}-${tokenQr.token}"
        val result = tokenQrUseCase.checkValidQR(validInput)
        Assert.assertEquals(
            expectedResult,
            (result as TokenQrUseCase.TokenQrResult.Success).uniqueCode
        )
    }


}