/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.view.View
import androidx.annotation.StringRes
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardHeaderBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class ButtonInfo(@StringRes val text: Int, @StringRes val link: Int)

class DashboardHeaderAdapterItem(@StringRes private val text: Int, private val buttonInfo: ButtonInfo?) :
    BindableItem<AdapterItemDashboardHeaderBinding>(R.layout.adapter_item_dashboard_header.toLong()), KoinComponent {

    private val intentUtil: IntentUtil by inject()

    override fun bind(viewBinding: AdapterItemDashboardHeaderBinding, position: Int) {
        viewBinding.text.setHtmlText(text, htmlLinksEnabled = true)
        viewBinding.button.run {
            if (buttonInfo != null) {
                visibility = View.VISIBLE
                setText(buttonInfo.text)
                setOnClickListener {
                    intentUtil.openUrl(
                        context = context,
                        url = context.getString(buttonInfo.link)
                    )
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.adapter_item_dashboard_header
    }

    override fun initializeViewBinding(view: View): AdapterItemDashboardHeaderBinding {
        return AdapterItemDashboardHeaderBinding.bind(view)
    }
}