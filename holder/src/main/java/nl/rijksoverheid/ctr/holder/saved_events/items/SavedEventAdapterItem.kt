/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemSavedEventBinding
import nl.rijksoverheid.ctr.holder.get_events.models.*
import nl.rijksoverheid.ctr.holder.saved_events.SavedEvents
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreen
import nl.rijksoverheid.ctr.shared.ext.capitalize

class SavedEventAdapterItem(
    private val savedEvent: SavedEvents.SavedEvent,
    private val onClick: (toolbarTitle: String, infoScreen: InfoScreen) -> Unit
): BindableItem<AdapterItemSavedEventBinding>() {

    override fun bind(viewBinding: AdapterItemSavedEventBinding, position: Int) {
        val context = viewBinding.root.context
        val title = when (savedEvent.remoteEvent) {
            is RemoteEventVaccination -> context.getString(R.string.general_vaccination).capitalize()
            is RemoteEventNegativeTest -> context.getString(R.string.general_negativeTest).capitalize()
            is RemoteEventPositiveTest -> context.getString(R.string.general_positiveTest).capitalize()
            is RemoteEventVaccinationAssessment -> context.getString(R.string.general_vaccinationAssessment).capitalize()
            is RemoteEventRecovery -> context.getString(R.string.general_recoverycertificate).capitalize()
            else -> ""
        }
        viewBinding.title.text = title
        viewBinding.subtitle.text = savedEvent.remoteEvent.getDate()?.toLocalDate()?.formatDayMonthYear()
        viewBinding.root.setOnClickListener {
            onClick.invoke(title, savedEvent.infoScreen)
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_saved_event
    }

    override fun initializeViewBinding(view: View): AdapterItemSavedEventBinding {
        return AdapterItemSavedEventBinding.bind(view)
    }
}