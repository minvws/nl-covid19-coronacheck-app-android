/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.models

sealed class DashboardSync {
    object ForceSync : DashboardSync()
    object DisableSync : DashboardSync()
    object CheckSync : DashboardSync()
}
