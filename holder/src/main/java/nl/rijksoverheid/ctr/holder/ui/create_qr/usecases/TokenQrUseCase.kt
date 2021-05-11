/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.TokenQR
import nl.rijksoverheid.ctr.shared.ext.toObject
import timber.log.Timber
import java.io.IOException

class TokenQrUseCase(private val moshi: Moshi) {

    fun checkValidQR(scannedQR: String): TokenQrResult {
        return try {
            val scannedData = scannedQR.toObject<TokenQR>(moshi)
            TokenQrResult.Success("${scannedData.providerIdentifier}-${scannedData.token}")
        } catch (e: IOException) {
            Timber.e(e)
            TokenQrResult.Failed
        } catch (e: JsonDataException) {
            Timber.e(e)
            TokenQrResult.Failed
        }
    }

    sealed class TokenQrResult {
        class Success(val uniqueCode: String) : TokenQrResult()
        object Failed : TokenQrResult()
    }
}
