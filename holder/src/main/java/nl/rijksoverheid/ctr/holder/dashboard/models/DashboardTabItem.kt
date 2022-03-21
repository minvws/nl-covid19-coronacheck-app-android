/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType

data class DashboardTabItem(
    @StringRes val title: Int,
    val greenCardType: GreenCardType,
    val items: List<DashboardItem>
)