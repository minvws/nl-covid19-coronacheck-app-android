package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.shared.util.QrCodeScannerUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GenerateHolderQrCodeUseCase(
    private val qrCodeScannerUtil: QrCodeScannerUtil
) {

    fun bitmap(
        data: String,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap {
        return qrCodeScannerUtil.createQrCode(
            data,
            qrCodeWidth,
            qrCodeHeight
        )
    }
}
