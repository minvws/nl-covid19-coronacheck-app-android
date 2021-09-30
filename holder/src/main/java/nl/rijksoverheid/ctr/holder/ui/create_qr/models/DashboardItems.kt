/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.models

sealed class DashboardErrorState {
    object None: DashboardErrorState()
    object RetryErrorState: DashboardErrorState()
    object HelpdeskErrorState: DashboardErrorState()
}

data class DashboardItems(
    val domesticItems: List<DashboardItem>,
    val internationalItems: List<DashboardItem>
)
