package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentQrCodeTypeBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeTypeFragment : Fragment(R.layout.fragment_qr_code_type) {

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentQrCodeTypeBinding.bind(view)

        binding.negativeTestButton.bind(
            R.string.qr_code_type_negative_test_title,
            getString(R.string.qr_code_type_negative_test_description)
        ) {
            if (cachedAppConfigUseCase.getCachedAppConfig()?.ggdEnabled == true) {
                findNavController().navigate(QrCodeTypeFragmentDirections.actionChooseProvider())
            } else {
                findNavController().navigate(QrCodeTypeFragmentDirections.actionCommercialTestCode())
            }
        }

        binding.recoveryButton.bind(
            R.string.qr_code_type_recovery_title,
            getString(R.string.qr_code_type_recovery_description)
        ) {
            findNavController().navigate(QrCodeTypeFragmentDirections.actionGetEvents(
                originType = OriginType.Recovery
            ))
        }

        binding.vaccinationButton.bind(
            R.string.qr_code_type_vaccination_title,
            getString(R.string.qr_code_type_vaccination_description)
        ) {
            findNavController().navigate(QrCodeTypeFragmentDirections.actionGetEvents(
                originType = OriginType.Vaccination
            ))
        }
    }
}
