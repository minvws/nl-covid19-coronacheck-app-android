/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.databinding.ActivityVerifierBinding
import nl.rijksoverheid.ctr.qrcode.QrCodeTools
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class VerifierActivity : AppCompatActivity() {

    private val qrCodeTools: QrCodeTools by inject()
    private val verifierViewModel: VerifierViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityVerifierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verifierViewModel.citizenAllowedLiveData.observe(this, Observer { citizenAllowedResult ->
            when (citizenAllowedResult) {
                is Result.Loading -> {
                    // TODO: Handle loading state
                }
                is Result.Success -> {
                    val customerAllowed = citizenAllowedResult.data
                    binding.root.setBackgroundColor(if (customerAllowed) Color.GREEN else Color.RED)
                }
                is Result.Failed -> {
                    Snackbar.make(
                        binding.root,
                        citizenAllowedResult.e.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

        binding.scan.setOnClickListener {
            onLaunchScanner()
        }
    }

    fun onLaunchScanner() {
        qrCodeTools.launchScanner(this) {
            verifierViewModel.validateCitizen(it)
        }
    }

    fun onLaunchScannerFake() {
        verifierViewModel.validateCitizen("{\"public_key\":\"X7iLCgWSppOucv6v/gb/EkFXDrWuISrAg3zbUMgu+RY=\",\"nonce\":\"nayU5cHhsn9s7ewQNZgxBbS28EnoG4nU\",\"payload\":\"WfhOu54nC8Br6RNGffYHFKdaIWd50FCCJ0ntqe1Hvhixp743YjIv1hR4/ZQBX3QUzfgpqIfQCy6hM6m2yDUTmAnqZAUU0Zh1tlINPPus1VGFpMX7DrW1ZneManVIawBRvergDW1VeqeuwZsqQF/t83fr/Pq2rtwf/7EPXQXxF1EmCuusT5jGYAs4dczErxrIzjinAnnS5Yo8D2c+/dJSJQdo6PdwjBD3Lo7gLEML7ISkRuV9+cyZrHz10yNe7VOMSMWfdzCrbDXCzoV28TA6PSwIE9rSTag/wlwSz0l40cV11sT8Fw6n3Xwb9ql6fpbFwdv/ksbMh/arPa27aR9YGJKBDZVm01/xB75O0nhZBelTVjfwS2qYbVo56lUaesZODyrl6CpR68LZEe2e2vIqucCUT+8lgGD4D8h/ZokLUKVb+6dvm04s/kysys0cRCWV\"}")
    }

}
