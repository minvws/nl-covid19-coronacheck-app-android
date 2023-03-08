/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.items

import android.view.View
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventsSectionBinding
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreen
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SavedEventsSectionAdapterItem(
    private val savedEvents: SavedEvents,
    private val onClickEvent: (toolbarTitle: String, infoScreen: InfoScreen) -> Unit,
    private val onClickClearData: (eventGroupEntity: EventGroupEntity) -> Unit
) : BindableItem<AdapterItemSavedEventsSectionBinding>(), KoinComponent {

    private val remoteEventUtil: RemoteEventUtil by inject()

    override fun bind(viewBinding: AdapterItemSavedEventsSectionBinding, position: Int) {
        setReceivedAt(
            viewBinding = viewBinding,
            providerName = savedEvents.providerName
        )
    }

    fun section(
        savedEvents: SavedEvents
    ): Section {

        val section = Section()

        val items = savedEvents.events.map {
            SavedEventAdapterItem(
                savedEvent = it,
                onClick = onClickEvent
            )
        }
        section.addAll(items)

        section.add(
            SavedEventsClearDataAdapterItem(
                eventGroupEntity = savedEvents.eventGroupEntity,
                onClick = onClickClearData
            )
        )

        return section
    }

    private fun setReceivedAt(
        viewBinding: AdapterItemSavedEventsSectionBinding,
        providerName: String
    ) {

        val context = viewBinding.root.context

        viewBinding.retrievedAt.text = if (remoteEventUtil.isDccEvent(providerName)) {
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
