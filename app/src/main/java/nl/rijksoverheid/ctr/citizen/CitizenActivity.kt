/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.citizen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.databinding.ActivityCitizenBinding
import nl.rijksoverheid.ctr.qrcode.QrCodeTools
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CitizenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenBinding
    private val qrCodeTools: QrCodeTools by inject()
    private val citizenViewModel: CitizenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCitizenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        citizenViewModel.userLiveData.observe(this, Observer { userResult ->
            when (userResult) {
                is Result.Success -> {
                    onUserLoggedInFake()
                }
                else -> {
                    // TODO: Handle non success state
                }
            }
        })

        citizenViewModel.qrCodeLiveData.observe(this, Observer { bitmapResult ->
            when (bitmapResult) {
                is Result.Success -> {
                    binding.login.visibility = View.GONE
                    binding.qrCode.setImageBitmap(bitmapResult.data)
                }
                else -> {
                    // TODO: Handle non success state
                }
            }
        })

        binding.qrCode.doOnPreDraw {
            binding.login.setOnClickListener {
                citizenViewModel.login()
            }
        }
    }

    private fun onUserLoggedIn() {
        qrCodeTools.launchScanner(this) {
            citizenViewModel.generateQrCode(it, binding.qrCode.width, binding.qrCode.height)
        }
    }

    private fun onUserLoggedInFake() {
        citizenViewModel.generateQrCode(
            "{\"event_signature\":\"qtIz9cPjDABMjI8P2d2AA3XyLLHqsFuPdlso8bPJVx1+0yXEBep6pFqniEGKVmo65\\/mldTtbK3xKM6eoxq0lCA==\",\"event\":{\"name\":\"Friday Night\",\"uuid\":\"d9ff36de-2357-4fa6-a64e-1569aa57bf1c\",\"public_key\":\"XlC1ybo\\/3yYQrd7g3+JhXDSk\\/QC\\/d0LKG5WKROQ7Mk8=\",\"valid_from\":1611008598,\"valid_to\":1611584139,\"location\":{\"uuid\":\"3b656099-da15-4bf9-8a6a-edcedd19ece3 \",\"name\":\"Centrum bar\",\"street_name\":\"Kettingstraat\",\"house_number\":3,\"zipcode\":\"2513AL\"},\"type\":{\"name\":\"Bar < 30 people\",\"uuid\":\"e2255ea4-2140-44c8-bdf0-33da60debf70\"},\"valid_tests\":[{\"name\":\"PCR\",\"uuid\":\"58d8e4b1-f890-4a2f-b810-0b775caa2149\",\"max_validity\":604800}]}}",
            binding.qrCode.width,
            binding.qrCode.height
        )
    }
}
