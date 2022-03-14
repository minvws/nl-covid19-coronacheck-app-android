/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardAddQrBinding
import org.koin.core.component.KoinComponent

class DashboardAddQrCardAdapterItem(
    private val onButtonClick: () -> Unit,
) : BindableItem<AdapterItemDashboardAddQrBinding>(R.layout.adapter_item_dashboard_add_qr.toLong()),
    KoinComponent {

    override fun bind(viewBinding: AdapterItemDashboardAddQrBinding, position: Int) {
        viewBinding.text.setOnClickListener { onButtonClick.invoke() }
    }

    override fun getLayout(): Int = R.layout.adapter_item_dashboard_add_qr

    override fun initializeViewBinding(view: View): AdapterItemDashboardAddQrBinding {
        return AdapterItemDashboardAddQrBinding.bind(view)
    }
}