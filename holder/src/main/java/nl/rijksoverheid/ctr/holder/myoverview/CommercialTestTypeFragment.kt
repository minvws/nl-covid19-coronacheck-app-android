package nl.rijksoverheid.ctr.holder.myoverview

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
import nl.rijksoverheid.ctr.holder.usecase.TokenQrUseCase
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
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
