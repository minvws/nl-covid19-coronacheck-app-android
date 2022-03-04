/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.widget.LinearLayout
import android.widget.TextView
import nl.rijksoverheid.ctr.holder.databinding.AdapterItemDashboardGreenCardBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DashboardGreenCardAdapterItemBindingWrapper {
    val title: TextView
    val description: LinearLayout
    val expiresIn: TextView
    val policyLabel: TextView
}

class DashboardGreenCardAdapterItemBindingWrapperImpl(private val viewBinding: AdapterItemDashboardGreenCardBinding) :
    DashboardGreenCardAdapterItemBindingWrapper {

    override val title: TextView
        get() = viewBinding.title

    override val description: LinearLayout
        get() = viewBinding.description

    override val expiresIn: TextView
        get() = viewBinding.expiresIn

    override val policyLabel: TextView
        get() = viewBinding.domesticPolicyLabel
}