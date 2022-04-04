/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventsHeaderBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SavedEventsHeaderAdapterItem: BindableItem<AdapterItemSavedEventsHeaderBinding>(), KoinComponent {

    private val intentUtil: IntentUtil by inject()

    override fun bind(viewBinding: AdapterItemSavedEventsHeaderBinding, position: Int) {
        val context = viewBinding.root.context

        viewBinding.button.setOnClickListener {
            intentUtil.openUrl(
                context = context,
                url = context.getString(R.string.holder_storedEvents_url)
            )
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_events_header
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventsHeaderBinding {
        return AdapterItemSavedEventsHeaderBinding.bind(view)
    }
}