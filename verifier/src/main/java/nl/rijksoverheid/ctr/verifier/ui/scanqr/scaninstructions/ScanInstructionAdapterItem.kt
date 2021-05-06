package nl.rijksoverheid.ctr.verifier.ui.scanqr.scaninstructions

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.ItemScanInstructionBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionAdapterItem(
    @StringRes private val title: Int,
    private val description: String,
    @DrawableRes private val image: Int? = null,
    private val imageDescription: String? = null,
) : BindableItem<ItemScanInstructionBinding>() {
    override fun bind(viewBinding: ItemScanInstructionBinding, position: Int) {
        viewBinding.title.setText(title)
        viewBinding.description.setHtmlTextWithBullets(
            htmlText = description,
            htmlLinksEnabled = true
        )

        if (image == null) {
            viewBinding.image.visibility = View.GONE
        } else {
            viewBinding.image.visibility = View.VISIBLE
            viewBinding.image.setImageResource(image)
            viewBinding.image.contentDescription = imageDescription
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_scan_instruction
    }

    override fun initializeViewBinding(view: View): ItemScanInstructionBinding {
        return ItemScanInstructionBinding.bind(view)
    }
}
