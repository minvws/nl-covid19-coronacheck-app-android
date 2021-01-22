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
import nl.rijksoverheid.ctr.citizen.CitizenActivity
import nl.rijksoverheid.ctr.databinding.ActivityMainBinding
import nl.rijksoverheid.ctr.verifier.VerifierActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.citizen.setOnClickListener {
            startActivity(Intent(this, CitizenActivity::class.java))
        }

        binding.verifier.setOnClickListener {
            startActivity(Intent(this, VerifierActivity::class.java))
        }
    }
}
