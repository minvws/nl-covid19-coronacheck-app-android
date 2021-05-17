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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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


        // Catch back button to show modal instead
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackButton()
            }
        })
    }


    private fun handleBackButton() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.retrieved_vaccinations_backbutton_title))
            .setMessage(getString(R.string.retrieved_vaccinations_backbutton_message))
            .setPositiveButton(R.string.your_negative_test_results_backbutton_ok) { _, _ ->
                findNavController().navigate(
                    YourRetrievedVaccinationsFragmentDirections.actionMyOverview()
                )
            }
            .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
            .show()
    }
}