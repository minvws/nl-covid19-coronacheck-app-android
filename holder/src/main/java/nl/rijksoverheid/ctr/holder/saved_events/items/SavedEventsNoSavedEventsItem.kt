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
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventsNoSavedEventsBinding

class SavedEventsNoSavedEventsItem : BindableItem<AdapterItemSavedEventsNoSavedEventsBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventsNoSavedEventsBinding, position: Int) {
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_events_no_saved_events
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventsNoSavedEventsBinding {
        return AdapterItemSavedEventsNoSavedEventsBinding.bind(view)
    }
}
