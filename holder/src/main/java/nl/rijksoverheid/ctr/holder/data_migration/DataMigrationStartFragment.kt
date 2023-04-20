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
import com.google.android.material.snackbar.Snackbar
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDataMigrationStartTransferringBinding
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataMigrationStartFragment : Fragment(R.layout.fragment_data_migration_start_transferring) {

    private val viewModel: DataMigrationStartViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDataMigrationStartTransferringBinding.bind(view)

        viewModel.canTransferData.observe(viewLifecycleOwner, EventObserver {
            binding.bottom.setButtonClick {
                if (it) {
                    navigateSafety(DataMigrationStartFragmentDirections.actionDataTransferOptions())
                } else {
                    Snackbar.make(binding.description, "No data to transfer", Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.canTransfer()
    }
}
