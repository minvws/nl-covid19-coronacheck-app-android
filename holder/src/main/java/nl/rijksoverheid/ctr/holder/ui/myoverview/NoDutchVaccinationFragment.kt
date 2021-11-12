package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDutchVaccinationCertificateBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NoDutchVaccinationFragment : Fragment(R.layout.fragment_no_dutch_vaccination_certificate) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentNoDutchVaccinationCertificateBinding.inflate(inflater)

        binding.vaccinationButton.setOnClickListener {
            navigate(OriginType.Vaccination)
        }

        binding.testButton.setOnClickListener {
            navigate(OriginType.Test)
        }

        binding.noDutchCertificateExplanationMore

        return binding.root
    }

    private fun navigate(originType: OriginType) {
        navigateSafety(NoDutchVaccinationFragmentDirections.actionGetEvents(originType))
    }
}