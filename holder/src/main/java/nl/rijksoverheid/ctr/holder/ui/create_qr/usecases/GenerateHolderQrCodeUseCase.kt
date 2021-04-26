package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.QrCodeUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GenerateHolderQrCodeUseCase(
    private val qrCodeUtil: QrCodeUtil
) {

    fun bitmap(
        data: String,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap {
        return qrCodeUtil.createQrCode(
            data,
            qrCodeWidth,
            qrCodeHeight
        )
    }
}
