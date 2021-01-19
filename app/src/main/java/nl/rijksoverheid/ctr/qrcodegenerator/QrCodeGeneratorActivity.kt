/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrcodegenerator

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import nl.rijksoverheid.ctr.databinding.ActivityQrCodeGeneratorBinding
import kotlin.random.Random


class QrCodeGeneratorActivity : AppCompatActivity() {

    private val alphaNumerics = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ%*+-./: "
    private val esLevels = ErrorCorrectionLevel.values().toList()
    private lateinit var binding: ActivityQrCodeGeneratorBinding
    private var currentContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.image.doOnPreDraw {
            add100KbContentToQrCode()
        }

        binding.addButton.setOnClickListener {
            add100KbContentToQrCode()
        }

        binding.removeButton.setOnClickListener {
            remove100KbContentFromQrCode()
        }

        val popupSpinnerAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, R.layout.simple_spinner_item, esLevels.map { it.name })
        popupSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.ecLevelSpinner.adapter = popupSpinnerAdapter
    }

    private fun add100KbContentToQrCode() {
        val randomString = List(100) { Random.nextInt(0, alphaNumerics.length) }
            .map { alphaNumerics[it] }
            .joinToString(separator = "")
        currentContent += randomString
        generateQrCode()
    }

    private fun remove100KbContentFromQrCode() {
        currentContent = currentContent.dropLast(100)
        generateQrCode()
    }

    @SuppressLint("SetTextI18n")
    private fun generateQrCode() {
        binding.amountText.text = "${currentContent.length} chars"
        val ecLevel = esLevels[binding.ecLevelSpinner.selectedItemPosition]
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(
            currentContent,
            BarcodeFormat.QR_CODE,
            binding.image.width,
            binding.image.height,
            mapOf(EncodeHintType.ERROR_CORRECTION to ecLevel)
        )
        binding.image.setImageBitmap(bitmap)
        binding.removeButton.isEnabled = currentContent.length > 100
    }

}
