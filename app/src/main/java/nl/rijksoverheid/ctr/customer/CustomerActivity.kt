/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.databinding.ActivityCustomerBinding

class CustomerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
