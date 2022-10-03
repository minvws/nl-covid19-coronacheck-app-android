/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentSavedEventsBinding
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsNoSavedEventsItem
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsSectionAdapterItem
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedEventsFragment : Fragment(R.layout.fragment_saved_events) {

    private val section = Section()
    private val savedEventsViewModel: SavedEventsViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSavedEventsBinding.bind(view)
        initRecyclerView(binding)

        listenToSavedEvents()
        listenToRemoveSavedEvents()
        savedEventsViewModel.getSavedEvents()
    }

    private fun initRecyclerView(binding: FragmentSavedEventsBinding) {
        section.clear()
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
    }

    private fun listenToSavedEvents() {
        savedEventsViewModel.savedEventsLiveData.observe(viewLifecycleOwner, EventObserver { savedEvents ->
            val items = mutableListOf<BindableItem<*>>()
            items.add(SavedEventsHeaderAdapterItem())

            if (savedEvents.isEmpty()) {
                items.add(SavedEventsNoSavedEventsItem())
            } else {
                savedEvents.forEach {
                    items.add(
                        SavedEventsSectionAdapterItem(
                            savedEvents = it,
                            onClickEvent = { toolbarTitle, infoScreen ->
                                navigateSafety(
                                    SavedEventsFragmentDirections.actionYourEventExplanation(
                                        toolbarTitle = toolbarTitle,
                                        data = arrayOf(infoScreen)
                                    )
                                )
                            },
                            onClickClearData = { eventGroupEntity ->
                                presentClearDataDialog {
                                    savedEventsViewModel.removeSavedEvents(eventGroupEntity)
                                }
                            }
                        )
                    )
                }
            }
            section.addAll(items)
        })
    }

    private fun listenToRemoveSavedEvents() {
        savedEventsViewModel.removedSavedEventsLiveData.observe(viewLifecycleOwner) {
            navigateSafety(
                SavedEventsFragmentDirections.actionSavedEventsSyncGreenCards()
            )
        }
    }

    private fun presentClearDataDialog(onClear: () -> Unit) {
        dialogUtil.presentDialog(
            context = requireContext(),
            title = R.string.holder_storedEvent_alert_removeEvents_title,
            message = getString(R.string.holder_storedEvent_alert_removeEvents_message),
            positiveButtonText = R.string.general_delete,
            negativeButtonText = R.string.general_cancel,
            positiveButtonCallback = {
                onClear.invoke()
            }
        )
    }
}
