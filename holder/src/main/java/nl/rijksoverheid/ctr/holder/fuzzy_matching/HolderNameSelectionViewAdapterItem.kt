package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemHolderNameSelectionViewBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionViewAdapterItem(
    private val item: HolderNameSelectionItem.ListItem,
    private val onSelected: (Int) -> Unit
) : BindableItem<ItemHolderNameSelectionViewBinding>(
    R.layout.item_holder_name_selection_view.toLong()
), KoinComponent {

    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val selectionDetailBottomSheetDescriptionUtil: SelectionDetailBottomSheetDescriptionUtil by inject()

    override fun bind(viewBinding: ItemHolderNameSelectionViewBinding, position: Int) {
        viewBinding.radioButton.isChecked = item.isSelected
        viewBinding.nameTextView.text = item.name
        viewBinding.eventsTextView.text = item.events
        viewBinding.removedEventTextView.isVisible = item.willBeRemoved
        viewBinding.root.setOnClickListener {
            onSelected(position)
        }
        viewBinding.detailsButton.setOnClickListener {
            onDetailsButtonClicked(viewBinding)
        }
    }

    private fun onDetailsButtonClicked(viewBinding: ItemHolderNameSelectionViewBinding) =
        (viewBinding.root.context as? AppCompatActivity)?.let { activity ->
            val nameText = activity.getString(R.string.holder_identitySelection_details_body, item.name)
            val eventsText = selectionDetailBottomSheetDescriptionUtil.get(
                selectionDetailData = item.detailData,
                separator = " ${activity.getString(R.string.general_and)} ") {
                activity.getString(R.string.holder_storedEvents_listHeader_fetchedFromProvider, it)
            }
            infoFragmentUtil.presentAsBottomSheet(
                fragmentManager = activity.supportFragmentManager,
                data = InfoFragmentData.TitleDescription(
                    title = activity.getString(R.string.general_details),
                    descriptionData = DescriptionData(
                        htmlTextString = "$nameText $eventsText",
                        htmlLinksEnabled = true
                    )
                )
            )
        }

    override fun getLayout(): Int {
        return R.layout.item_holder_name_selection_view
    }

    override fun initializeViewBinding(view: View): ItemHolderNameSelectionViewBinding {
        return ItemHolderNameSelectionViewBinding.bind(view)
    }
}
