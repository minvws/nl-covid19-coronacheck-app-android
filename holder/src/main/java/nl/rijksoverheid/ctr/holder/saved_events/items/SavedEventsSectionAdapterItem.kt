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
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

class SavedEventsSectionAdapterItem(
    private val savedEvents: SavedEvents,
    private val onClickEvent: (isDccEvent: Boolean, providerIdentifier: String, holder: RemoteProtocol3.Holder?, remoteEvent: RemoteEvent) -> Unit,
    private val onClickClearData: (eventGroupEntity: EventGroupEntity) -> Unit
): BindableItem<AdapterItemSavedEventsSectionBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventsSectionBinding, position: Int) {
        setReceivedAt(
            viewBinding = viewBinding,
            providerName = savedEvents.providerName
        )
        initRecyclerView(
            viewBinding = viewBinding,
            remoteProtocol = savedEvents.remoteProtocol3
        )
    }

    private fun initRecyclerView(
        viewBinding: AdapterItemSavedEventsSectionBinding,
        remoteProtocol: RemoteProtocol3) {

        val section = Section()
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        viewBinding.recyclerView.adapter = adapter
        viewBinding.recyclerView.itemAnimator = null

        remoteProtocol.events?.let {
            val items = it.map { remoteEvent ->
                SavedEventAdapterItem(
                    isDccEvent = savedEvents.isDccEvent,
                    providerIdentifier = savedEvents.providerIdentifier,
                    holder = remoteProtocol.holder,
                    remoteEvent = remoteEvent,
                    onClick = onClickEvent
                )
            }
            section.addAll(items)
        }

        section.add(
            SavedEventsClearDataAdapterItem(
                eventGroupEntity = savedEvents.eventGroupEntity,
                onClick = onClickClearData
            )
        )
    }

    private fun setReceivedAt(
        viewBinding: AdapterItemSavedEventsSectionBinding,
        providerName: String) {

        val context = viewBinding.root.context

        viewBinding.retrievedAt.text = if (savedEvents.providerName == "DCC") {
            context.getString(R.string.holder_storedEvents_listHeader_paperFlow)
        } else {
            context.getString(R.string.holder_storedEvents_listHeader_fetchedFromProvider, providerName)
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_events_section
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventsSectionBinding {
        return AdapterItemSavedEventsSectionBinding.bind(view)
    }
}