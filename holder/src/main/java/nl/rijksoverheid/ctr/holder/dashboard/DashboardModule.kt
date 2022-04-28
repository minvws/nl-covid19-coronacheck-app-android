/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import nl.rijksoverheid.ctr.holder.dashboard.datamappers.DashboardTabsItemDataMapper
import nl.rijksoverheid.ctr.holder.dashboard.datamappers.DashboardTabsItemDataMapperImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageAccessibilityUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageAccessibilityUtilImpl
import org.koin.dsl.module

val dashboardModule = module {
    factory<DashboardTabsItemDataMapper> { DashboardTabsItemDataMapperImpl(get()) }
    factory<DashboardPageAccessibilityUtil> { DashboardPageAccessibilityUtilImpl() }
}