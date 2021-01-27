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
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import nl.rijksoverheid.ctr.databinding.ActivityCitizenBinding
import nl.rijksoverheid.ctr.shared.models.Result
import org.koin.android.viewmodel.ext.android.viewModel

class CitizenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenBinding
    private val citizenViewModel: CitizenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCitizenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        citizenViewModel.qrCodeLiveData.observe(this, Observer { bitmapResult ->
            when (bitmapResult) {
                is Result.Loading -> {
                    // TODO: Handle loading state
                }
                is Result.Success -> {
                    binding.login.visibility = View.GONE
                    binding.qrCode.setImageBitmap(bitmapResult.data)
                }
                is Result.Failed -> {
                    Snackbar.make(binding.root, bitmapResult.e.toString(), Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        })

        binding.login.setOnClickListener {
            citizenViewModel.generateQrCode(
                activity = this,
                qrCodeWidth = binding.qrCode.width,
                qrCodeHeight = binding.qrCode.height
            )
        }
    }
}
