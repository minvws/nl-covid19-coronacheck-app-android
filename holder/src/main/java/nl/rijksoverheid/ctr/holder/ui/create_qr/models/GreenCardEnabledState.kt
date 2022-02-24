/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import nl.rijksoverheid.ctr.holder.R

sealed class GreenCardEnabledState {
    object Enabled: GreenCardEnabledState()
    data class Disabled(val text: Int = R.string.holder_dashboard_domesticQRCard_3G_inactive_label): GreenCardEnabledState()
}