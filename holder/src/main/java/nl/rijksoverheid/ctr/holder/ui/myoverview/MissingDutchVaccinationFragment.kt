package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMissingDutchVaccinationCertificateBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MissingDutchVaccinationFragment :
    Fragment(R.layout.fragment_missing_dutch_vaccination_certificate) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentMissingDutchVaccinationCertificateBinding.inflate(inflater)

        binding.vaccinationButton.setOnClickListener {
            navigate(RemoteOriginType.Vaccination)
        }

        binding.testButton.setOnClickListener {
            navigate(RemoteOriginType.Recovery, afterIncompleteVaccination = true)
        }

        return binding.root
    }

    private fun navigate(originType: RemoteOriginType, afterIncompleteVaccination: Boolean = false) {
        navigateSafety(
            MissingDutchVaccinationFragmentDirections.actionGetEvents(
                originType = originType,
                afterIncompleteVaccination = afterIncompleteVaccination,
                toolbarTitle = resources.getString(
                    if (afterIncompleteVaccination) R.string.retrieve_test_result_toolbar_title else R.string.choose_provider_toolbar
                )
            )
        )
    }
}