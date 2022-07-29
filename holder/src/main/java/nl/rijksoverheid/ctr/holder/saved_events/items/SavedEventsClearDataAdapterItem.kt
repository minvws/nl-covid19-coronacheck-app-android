/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventsClearDataBinding
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

class SavedEventsClearDataAdapterItem(
    private val eventGroupEntity: EventGroupEntity,
    private val onClick: (eventGroupEntity: EventGroupEntity) -> Unit
) : BindableItem<AdapterItemSavedEventsClearDataBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventsClearDataBinding, position: Int) {
        viewBinding.root.setOnClickListener {
            onClick.invoke(eventGroupEntity)
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_events_clear_data
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventsClearDataBinding {
        return AdapterItemSavedEventsClearDataBinding.bind(view)
    }
}
