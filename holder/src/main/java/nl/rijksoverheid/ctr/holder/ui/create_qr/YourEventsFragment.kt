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
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.YourEventWidget
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.scope.emptyState

class YourEventsFragment : Fragment(R.layout.fragment_your_events) {

    private val eventViewModel: EventViewModel by sharedViewModelWithOwner(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_get_vaccination),
                this
            )
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentYourEventsBinding.bind(view)

        val retrievedResult = eventViewModel.getRetrievedResult()
        if (retrievedResult == null) {
            findNavController().navigate(YourEventsFragmentDirections.actionMyOverview())
        } else {
            retrievedResult.vaccinationEvents.forEachIndexed { index, event ->
                val eventWidget = YourEventWidget(requireContext()).also {
                    it.setContent(
                        position = index + 1,
                        date = event.vaccination.date,
                        infoClickListener = {
                            findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation())
                        }
                    )
                }
                binding.eventsGroup.addView(eventWidget)
            }
        }

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
                    YourEventsFragmentDirections.actionMyOverview()
                )
            }
            .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
            .show()
    }
}
