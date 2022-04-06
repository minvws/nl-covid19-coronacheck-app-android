/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventBinding
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.capitalize

class SavedEventAdapterItem(
    private val savedEvent: SavedEvents.SavedEvent
): BindableItem<AdapterItemSavedEventBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventBinding, position: Int) {
        val context = viewBinding.root.context
        val title = when (savedEvent.type) {
            OriginType.Recovery -> context.getString(R.string.general_positiveTest).capitalize()
            OriginType.Test -> context.getString(R.string.general_negativeTest).capitalize()
            OriginType.Vaccination -> context.getString(R.string.general_vaccination).capitalize()
            OriginType.VaccinationAssessment -> context.getString(R.string.general_vaccinationAssessment).capitalize()
        }
        viewBinding.title.text = title
        viewBinding.subtitle.text = savedEvent.date.formatDateTime(context)
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_event
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventBinding {
        return AdapterItemSavedEventBinding.bind(view)
    }
}