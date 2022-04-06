/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.items

import android.view.View
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventsSectionBinding
import nl.rijksoverheid.ctr.holder.databinding.FragmentSavedEventsBinding
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

class SavedEventsSectionAdapterItem(
    private val savedEvents: SavedEvents,
    private val onClickClearData: (eventGroupEntity: EventGroupEntity) -> Unit
): BindableItem<AdapterItemSavedEventsSectionBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventsSectionBinding, position: Int) {
        setReceivedAt(
            viewBinding = viewBinding,
            provider = savedEvents.provider
        )
        initRecyclerView(
            viewBinding = viewBinding,
            events = savedEvents.events
        )
    }

    private fun initRecyclerView(
        viewBinding: AdapterItemSavedEventsSectionBinding,
        events: List<SavedEvents.SavedEvent>) {

        val section = Section()
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        viewBinding.recyclerView.adapter = adapter
        viewBinding.recyclerView.itemAnimator = null
        val items = events.map {
            SavedEventAdapterItem(it)
        }
        section.addAll(items)
        section.add(
            SavedEventsClearDataAdapterItem(
                eventGroupEntity = savedEvents.eventGroupEntity,
                onClick = onClickClearData
            )
        )
    }

    private fun setReceivedAt(
        viewBinding: AdapterItemSavedEventsSectionBinding,
        provider: String) {

        val context = viewBinding.root.context

        viewBinding.retrievedAt.text = if (savedEvents.provider == "DCC") {
            context.getString(R.string.holder_storedEvents_listHeader_paperFlow)
        } else {
            context.getString(R.string.holder_storedEvents_listHeader_fetchedFromProvider, provider)
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_events_section
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventsSectionBinding {
        return AdapterItemSavedEventsSectionBinding.bind(view)
    }
}