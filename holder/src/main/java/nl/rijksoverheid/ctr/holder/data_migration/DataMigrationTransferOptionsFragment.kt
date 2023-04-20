/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDataMigrationTransferOptionsBinding

class DataMigrationTransferOptionsFragment : Fragment(R.layout.fragment_data_migration_transfer_options) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDataMigrationTransferOptionsBinding.bind(view)

        binding.transferOutButton.transferOptionsButtonTitle.text = getString(R.string.holder_startMigration_option_toOtherDevice_title)
        binding.transferOutButton.transferOptionsButtonIcon.setImageResource(R.drawable.transfer_out)
        binding.transferInButton.transferOptionsButtonTitle.text = getString(R.string.holder_startMigration_option_toThisDevice_title)
        binding.transferInButton.transferOptionsButtonIcon.setImageResource(R.drawable.transfer_in)
    }
}
