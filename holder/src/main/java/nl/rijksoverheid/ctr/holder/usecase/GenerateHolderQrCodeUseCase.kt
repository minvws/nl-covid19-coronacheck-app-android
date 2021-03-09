package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GenerateHolderQrCodeUseCase {

    fun bitmap(
        data: String,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(
            EncodeHintType::class.java
        )
        hints[EncodeHintType.MARGIN] = 0
        val bitMatrix = multiFormatWriter.encode(
            data,
            BarcodeFormat.QR_CODE,
            qrCodeWidth,
            qrCodeHeight,
            hints
        )
        val bitmap = Bitmap.createBitmap(
            qrCodeWidth,
            qrCodeHeight,
            Bitmap.Config.RGB_565
        )
        for (i in 0 until qrCodeWidth) {
            for (j in 0 until qrCodeHeight) {
                bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
