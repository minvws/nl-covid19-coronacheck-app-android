package nl.rijksoverheid.ctr.holder.myoverview.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewTestResultBinding
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTestResultAdapterItem(private val localTestResultValid: LocalTestResultState) :
    BindableItem<ItemMyOverviewTestResultBinding>() {
    override fun bind(viewBinding: ItemMyOverviewTestResultBinding, position: Int) {
        val context = viewBinding.root.context

        when (localTestResultValid) {
            is LocalTestResultState.QrCode -> {
                viewBinding.testResultLoading.visibility = View.GONE
                viewBinding.testResultQrImage.setImageBitmap(localTestResultValid.qrCode)
                viewBinding.testResultFooter.text = context.getString(
                    R.string.my_overview_existing_qr_date,
                    localTestResultValid.localTestResult.expireDate.format(
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    )
                )

            }
            is LocalTestResultState.Valid -> {
                viewBinding.testResultLoading.visibility = View.VISIBLE
                viewBinding.testResultFooter.text = context.getString(
                    R.string.my_overview_existing_qr_date,
                    localTestResultValid.localTestResult.expireDate.format(
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    )
                )
            }
            else -> {
                // Nothing
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_test_result
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewTestResultBinding {
        return ItemMyOverviewTestResultBinding.bind(view)
    }
}
