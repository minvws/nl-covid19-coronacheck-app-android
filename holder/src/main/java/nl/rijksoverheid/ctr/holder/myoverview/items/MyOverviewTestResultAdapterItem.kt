package nl.rijksoverheid.ctr.holder.myoverview.items

import android.graphics.Bitmap
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewTestResultBinding
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTestResultAdapterItem(
    private val localTestResult: LocalTestResult,
    private val qrCode: Bitmap? = null
) :
    BindableItem<ItemMyOverviewTestResultBinding>(R.layout.item_my_overview_test_result.toLong()) {
    override fun bind(viewBinding: ItemMyOverviewTestResultBinding, position: Int) {
        val context = viewBinding.root.context

        viewBinding.testResultFooter.text = context.getString(
            R.string.my_overview_existing_qr_date,
            localTestResult.expireDate.format(
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            )
        )

        if (qrCode == null) {
            viewBinding.testResultLoading.visibility = View.VISIBLE
        } else {
            viewBinding.testResultLoading.visibility = View.GONE
            viewBinding.testResultQrImage.setImageBitmap(qrCode)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_test_result
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewTestResultBinding {
        return ItemMyOverviewTestResultBinding.bind(view)
    }
}
