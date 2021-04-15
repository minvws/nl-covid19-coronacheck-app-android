package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestTypeBinding
import nl.rijksoverheid.ctr.holder.databinding.IncludeTestCodeTypeBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.usecase.TokenQrUseCase
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CommercialTestTypeFragment : Fragment(R.layout.fragment_commercial_test_type) {

    private val tokenQrViewModel: TokenQrViewModel by viewModel()
    private val persistenceManager: PersistenceManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCommercialTestTypeBinding.bind(view)
        binding.typeCode.bind(R.drawable.ic_test_code, R.string.commercial_test_type_code_title) {
            findNavController().navigate(CommercialTestTypeFragmentDirections.actionCommercialTestCode())
        }
        binding.typeQrCode.bind(
            R.drawable.ic_test_qr_code,
            R.string.commercial_test_type_qr_code_title
        ) {
            if (persistenceManager.hasSeenCameraRationale() == true) {

            } else {
                showCameraRationale()
            }
        }
        binding.noCodeButton.setOnClickListener {
            findNavControllerSafety(R.id.nav_commercial_test_type)?.navigate(
                CommercialTestTypeFragmentDirections.actionNoCode()
            )
        }

        tokenQrViewModel.locationData.observe(
            viewLifecycleOwner,
            EventObserver { qrScanResult ->
                if (qrScanResult is TokenQrUseCase.TokenQrResult.Success) {
                    // Navigate to regular code fill-in fragment, supplying the code we received from our scanned QR code
                    findNavController().navigate(
                        CommercialTestTypeFragmentDirections.actionCommercialTestCode(
                            qrScanResult.uniqueCode
                        )
                    )
                } else if (qrScanResult is TokenQrUseCase.TokenQrResult.Failed) {
                    // show alert if code is invalid
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.commercial_test_invalid_qr_title))
                        .setMessage(getString(R.string.commercial_test_invalid_qr_message))
                        .setPositiveButton(R.string.ok) { _, _ -> }
                        .show()
                }
            })
    }

    private fun showCameraRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.camera_rationale_dialog_title))
            .setMessage(getString(R.string.camera_rationale_dialog_description))
            .setPositiveButton(R.string.camera_rationale_dialog_accept) { _, _ ->
                persistenceManager.setHasSeenCameraRationale(true)
            }
            .setNegativeButton(R.string.camera_rationale_dialog_deny) { _, _ -> }
            .show()
    }
}

private fun IncludeTestCodeTypeBinding.bind(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit
) {
    this.icon.setImageResource(icon)
    this.title.setText(title)
    root.setOnClickListener {
        onClick()
    }
}
