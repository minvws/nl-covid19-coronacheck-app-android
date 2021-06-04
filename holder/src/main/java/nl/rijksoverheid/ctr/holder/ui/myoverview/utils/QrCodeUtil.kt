package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeUtil {
    fun createQrCode(qrCodeContent: String, width: Int, height: Int, errorCorrectionLevel: ErrorCorrectionLevel): Bitmap
}

class QrCodeUtilImpl : QrCodeUtil {
    override fun createQrCode(qrCodeContent: String, width: Int, height: Int, errorCorrectionLevel: ErrorCorrectionLevel): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(
            EncodeHintType::class.java
        )
        hints[EncodeHintType.MARGIN] = 0
        hints[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
        val bitMatrix = multiFormatWriter.encode(
            qrCodeContent,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.RGB_565
        )
        for (i in 0 until width) {
            for (j in 0 until height) {
                bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
