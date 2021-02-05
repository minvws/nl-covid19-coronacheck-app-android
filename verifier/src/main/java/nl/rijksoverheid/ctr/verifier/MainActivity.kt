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
import nl.rijksoverheid.ctr.shared.util.QrCodeUtils
import nl.rijksoverheid.ctr.verifier.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val qrCodeUtils: QrCodeUtils by inject()
    private val verifierViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verifierViewModel.holderAllowedLiveData.observe(this, Observer { holderAllowedResult ->
            when (holderAllowedResult) {
                is nl.rijksoverheid.ctr.shared.models.Result.Loading -> {
                    // TODO: Handle loading state
                }
                is nl.rijksoverheid.ctr.shared.models.Result.Success -> {
                    val customerAllowed = holderAllowedResult.data
                    binding.root.setBackgroundColor(if (customerAllowed) Color.GREEN else Color.RED)
                }
                is nl.rijksoverheid.ctr.shared.models.Result.Failed -> {
                    Snackbar.make(
                        binding.root,
                        holderAllowedResult.e.toString(),
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
        qrCodeUtils.launchScanner(this) {
            verifierViewModel.validateholder(it)
        }
    }
}
