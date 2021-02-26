/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecase

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.myoverview.models.LocationQrData
import nl.rijksoverheid.ctr.shared.ext.toObject
import timber.log.Timber
import java.lang.Exception

class CheckLocationQrUseCase(private val moshi: Moshi) {

    fun checkValidQR(scannedQR : String) : QrCheckResult{
        return try {
            val scannedData = scannedQR.toObject<LocationQrData>(moshi)
            QrCheckResult.Success(scannedData)
        }catch (e:Exception){
            Timber.e(e)
            QrCheckResult.Failed
        }
    }

    sealed class QrCheckResult {
        class Success(val locationQrData: LocationQrData) : QrCheckResult()
        object Failed : QrCheckResult()
    }
}