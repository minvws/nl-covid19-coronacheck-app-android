package nl.rijksoverheid.ctr.shared.util

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeScannerUtil {
    fun launchScanner(activity: Activity, activityResultLauncher: ActivityResultLauncher<Intent>)
    fun createQrCode(qrCodeContent: String, width: Int, height: Int): Bitmap
}

class ZxingQrCodeScannerUtil : QrCodeScannerUtil {
    override fun launchScanner(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val integrator = IntentIntegrator(activity)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        activityResultLauncher.launch(integrator.createScanIntent())
    }

    override fun createQrCode(qrCodeContent: String, width: Int, height: Int): Bitmap {
        // TODO: Use correct es level after user tests
        val ecLevel = ErrorCorrectionLevel.values().first()
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(
            qrCodeContent,
            BarcodeFormat.QR_CODE,
            width,
            height,
            mapOf(EncodeHintType.ERROR_CORRECTION to ecLevel)
        )
    }

}
