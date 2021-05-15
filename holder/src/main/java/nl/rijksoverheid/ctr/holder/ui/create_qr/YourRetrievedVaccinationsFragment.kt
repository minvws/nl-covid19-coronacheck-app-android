/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentRetrievedVaccinationsBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.RetrievedVaccinationWidget
import java.time.OffsetDateTime

class YourRetrievedVaccinationsFragment : Fragment(R.layout.fragment_retrieved_vaccinations) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRetrievedVaccinationsBinding.bind(view)
        val dummyItem = RetrievedVaccinationWidget(requireContext()).also {
            it.setContent(
                position = 1,
                date = OffsetDateTime.now(),
                infoClickListener = {
                    findNavController().navigate(YourRetrievedVaccinationsFragmentDirections.actionShowExplanation())
                }
            )
        }

        binding.vaccinationResultsGroup.addView(dummyItem)
    }
}