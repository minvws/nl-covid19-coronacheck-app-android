/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.customer.CustomerActivity
import nl.rijksoverheid.ctr.databinding.ActivityMainBinding
import nl.rijksoverheid.ctr.qrcodegenerator.QrCodeGeneratorActivity
import nl.rijksoverheid.ctr.qrcodescanner.QrCodeScannerActivity
import nl.rijksoverheid.ctr.verifier.VerifierActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.customer.setOnClickListener {
            startActivity(Intent(this, CustomerActivity::class.java))
        }

        binding.verifier.setOnClickListener {
            startActivity(Intent(this, VerifierActivity::class.java))
        }

        binding.qrCodeGenerator.setOnClickListener {
            startActivity(Intent(this, QrCodeGeneratorActivity::class.java))
        }

        binding.qrCodeScanner.setOnClickListener {
            startActivity(Intent(this, QrCodeScannerActivity::class.java))
        }
    }
}
