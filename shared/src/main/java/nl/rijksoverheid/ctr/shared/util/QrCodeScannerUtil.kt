package nl.rijksoverheid.ctr.shared.util

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.result.ActivityResultLauncher
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerActivity
import java.util.*


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeScannerUtil {
    fun launchScanner(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTitle: String,
        customMessage: String,
        rationaleDialogTitle: String? = null,
        rationaleDialogDescription: String? = null,
        rationaleDialogOkayButtonText: String? = null
    )

    fun createQrCode(qrCodeContent: String, width: Int, height: Int): Bitmap

    fun parseScanResult(resultIntent: Intent?): String?
}

class MLKitQrCodeScannerUtil : QrCodeScannerUtil {
    override fun launchScanner(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTitle: String,
        customMessage: String,
        rationaleDialogTitle: String?,
        rationaleDialogDescription: String?,
        rationaleDialogOkayButtonText: String?
    ) {
        val rationaleDialog =
            if (rationaleDialogTitle != null && rationaleDialogDescription != null && rationaleDialogOkayButtonText != null) QrCodeScannerActivity.RationaleDialog(
                rationaleDialogTitle,
                rationaleDialogDescription,
                rationaleDialogOkayButtonText
            ) else null
        val intentScan = QrCodeScannerActivity.getIntent(
            context = activity,
            customTitle = customTitle,
            customMessage = customMessage,
            rationaleDialog = rationaleDialog
        )
        activityResultLauncher.launch(intentScan)
    }

    override fun createQrCode(qrCodeContent: String, width: Int, height: Int): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(
            EncodeHintType::class.java
        )
        hints[EncodeHintType.MARGIN] = 0
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

    override fun parseScanResult(resultIntent: Intent?): String? {
        resultIntent?.extras?.let { bun ->
            if (bun.containsKey(QrCodeScannerActivity.SCAN_RESULT)) {
                return bun.getString(QrCodeScannerActivity.SCAN_RESULT)!!
            }
        }
        return null
    }
}
