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
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsNoSavedEventsItem
import nl.rijksoverheid.ctr.holder.saved_events.items.SavedEventsSectionAdapterItem
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedEventsFragment: Fragment(R.layout.fragment_saved_events) {

    private val section = Section()
    private val savedEventsViewModel: SavedEventsViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()
    private val infoScreenUtil: InfoScreenUtil by inject()
    private val yourEventsFragmentUtil: YourEventsFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSavedEventsBinding.bind(view)
        initRecyclerView(binding)

        listenToSavedEvents()
        listenToRemoveSavedEvents()
        savedEventsViewModel.getSavedEvents()
    }

    private fun initRecyclerView(binding: FragmentSavedEventsBinding) {
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
                            onClickEvent = { isDccEvent, providerIdentifier, holder, remoteEvent ->
                                showEventDetail(isDccEvent, providerIdentifier, holder, remoteEvent)
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

    private fun showEventDetail(isDccEvent: Boolean, providerIdentifier: String, holder: RemoteProtocol3.Holder?, remoteEvent: RemoteEvent) {
        val fullName = yourEventsFragmentUtil.getFullName(holder)
        val birthDate = yourEventsFragmentUtil.getBirthDate(holder)

        when (remoteEvent) {
            is RemoteEventVaccination -> showEventDetailForVaccination(isDccEvent, providerIdentifier, remoteEvent, fullName, birthDate)
        }
    }

    private fun showEventDetailForVaccination(
        isDccEvent: Boolean,
        providerIdentifier: String,
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String
    ) {
        val infoScreen = infoScreenUtil.getForVaccination(
            event = event,
            fullName = fullName,
            birthDate = birthDate,
            providerIdentifier = providerIdentifier,
            isPaperProof = isDccEvent
        )

        navigateSafety(
            SavedEventsFragmentDirections.actionYourEventExplanation(
                toolbarTitle = getString(R.string.your_test_result_explanation_toolbar_title),
                data = arrayOf(infoScreen)
            )
        )
    }
}