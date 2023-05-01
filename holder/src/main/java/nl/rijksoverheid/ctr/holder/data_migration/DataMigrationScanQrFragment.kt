/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataMigrationScanQrFragment : QrCodeScannerFragment() {

    private val viewModel: DataMigrationScanQrViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scanFinishedLiveData.observe(viewLifecycleOwner, EventObserver {
            navigateSafety(
                DataMigrationScanQrFragmentDirections.actionYourEvents(
                    type = it,
                    toolbarTitle = "",
                    flow = HolderFlow.VaccinationAndPositiveTest
                )
            )
        })

        viewModel.progressBarLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = true
            binding.progressBar.progress = it.calculateProgressPercentage()
        }
    }

    override fun onQrScanned(content: String) {
        binding.extraContentTitle.text =
            getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_titleScanning)
        binding.extraContentMessage.text =
            getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_messageKeepPointing)
        viewModel.onQrScanned(content)
        setupScanner()
    }

    override fun getCopy(): Copy {
        return Copy(
            title = getString(R.string.add_paper_proof_qr_scanner_title),
            message = "",
            extraContent = Copy.ExtraContent(
                header = getString(R.string.holder_startMigration_onboarding_step, "3"),
                title = getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_title),
                message = getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_message)
            )
        )
    }
}
