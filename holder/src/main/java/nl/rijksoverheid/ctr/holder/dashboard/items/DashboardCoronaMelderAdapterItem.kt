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
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardCoronaMelderBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DashboardCoronaMelderAdapterItem: BindableItem<AdapterItemDashboardCoronaMelderBinding>(R.layout.adapter_item_dashboard_corona_melder.toLong()) {
    override fun bind(viewBinding: AdapterItemDashboardCoronaMelderBinding, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_dashboard_corona_melder
    }

    override fun initializeViewBinding(view: View): AdapterItemDashboardCoronaMelderBinding {
        return AdapterItemDashboardCoronaMelderBinding.bind(view)
    }
}