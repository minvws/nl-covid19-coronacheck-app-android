/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.choose_proof_type

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProofTypeBinding
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChooseProofTypeFragment : Fragment(R.layout.fragment_choose_proof_type) {

    private val featureFlagUseCase: HolderFeatureFlagUseCase by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentChooseProofTypeBinding.bind(view)

        binding.negativeTestButton.bind(
            R.string.qr_code_type_negative_test_title,
            getString(R.string.qr_code_type_negative_test_description)
        ) {
            if (featureFlagUseCase.getGgdEnabled()) {
                findNavController().navigate(ChooseProofTypeFragmentDirections.actionChooseProvider())
            } else {
                findNavController().navigate(ChooseProofTypeFragmentDirections.actionInputToken())
            }
        }

        binding.recoveryButton.bind(
            R.string.qr_code_type_recovery_title,
            getString(R.string.qr_code_type_recovery_description)
        ) {
            findNavController().navigate(
                ChooseProofTypeFragmentDirections.actionGetEvents(
                    originType = RemoteOriginType.Recovery,
                    toolbarTitle = resources.getString(R.string.choose_provider_toolbar)
                )
            )
        }

        binding.vaccinationButton.bind(
            R.string.qr_code_type_vaccination_title,
            getString(R.string.qr_code_type_vaccination_description)
        ) {
            findNavController().navigate(
                ChooseProofTypeFragmentDirections.actionGetEvents(
                    originType = RemoteOriginType.Vaccination,
                    toolbarTitle = resources.getString(R.string.choose_provider_toolbar)
                )
            )
        }
    }
}
