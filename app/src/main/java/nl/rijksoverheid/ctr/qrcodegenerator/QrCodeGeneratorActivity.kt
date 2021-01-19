/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrcodegenerator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import nl.rijksoverheid.ctr.databinding.ActivityQrCodeGeneratorBinding
import kotlin.random.Random

class QrCodeGeneratorActivity : AppCompatActivity() {

    private val alphaNumerics = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ%*+-./: "
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
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(
            currentContent,
            BarcodeFormat.QR_CODE,
            binding.image.width,
            binding.image.height
        )
        binding.image.setImageBitmap(bitmap)
    }

}
