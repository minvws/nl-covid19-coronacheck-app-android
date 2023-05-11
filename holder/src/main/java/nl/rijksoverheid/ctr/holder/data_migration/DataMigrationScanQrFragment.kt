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
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataMigrationScanQrFragment : QrCodeScannerFragment() {

    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()

    private val viewModel: DataMigrationScanQrViewModel by viewModel()

    private val errorCodeStringFactory: ErrorCodeStringFactory by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scanFinishedLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavControllerSafety()?.popBackStack()

            when (it) {
                is DataMigrationScanQrState.Error -> holderMainActivityViewModel.navigateWithBundle(
                    DataMigrationInstructionsFragmentDirections.actionErrorResult().actionId,
                    ErrorResultFragment.getBundle(
                        ErrorResultFragmentData(
                            title = getString(R.string.error_something_went_wrong_title),
                            description = getString(
                                R.string.holder_migration_errorcode_message,
                                errorCodeStringFactory.get(
                                    HolderFlow.Migration,
                                    listOf(it.errorResult)
                                )
                            ),
                            buttonTitle = getString(R.string.general_toMyOverview),
                            buttonAction = ErrorResultFragmentData.ButtonAction.Destination(
                                R.id.action_my_overview
                            )
                        )
                    )
                )
                is DataMigrationScanQrState.Success -> holderMainActivityViewModel.navigate(
                    navDirections = DataMigrationInstructionsFragmentDirections.actionYourEvents(
                        type = it.type,
                        toolbarTitle = "",
                        flow = HolderFlow.Migration
                    )
                )
            }
        })

        viewModel.progressBarLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = true
            binding.progressBar.progress = it.calculateProgressPercentage()
        }

        (parentFragment?.parentFragment as? HolderMainFragment)?.getToolbar()?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment?.parentFragment as? HolderMainFragment)?.getToolbar()?.isVisible = true
    }

    override fun onQrScanned(content: String) {
        binding.extraContentTitle.text =
            getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_titleScanning)
        binding.extraContentMessage.text =
            getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_messageKeepPointing)
        viewModel.onQrScanned(content)
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
