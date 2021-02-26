package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestTypeBinding
import nl.rijksoverheid.ctr.holder.databinding.IncludeTestCodeTypeBinding
import nl.rijksoverheid.ctr.shared.util.MLKitQrCodeScannerUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CommercialTestTypeFragment : Fragment(R.layout.fragment_commercial_test_type) {

    private val qrCodeScannerUtil: MLKitQrCodeScannerUtil by inject()

    private val qrScanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val scanResult = qrCodeScannerUtil.parseScanResult(it.data)
            if (scanResult != null) {
                Timber.d("Got scan result $scanResult")
            }
        }

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
            qrCodeScannerUtil.launchScanner(requireActivity() as AppCompatActivity, qrScanResult)
        }
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