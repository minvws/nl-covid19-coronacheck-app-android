package nl.rijksoverheid.ctr.holder.ui.myoverview.models

sealed class DashboardSync {
    object ForceSync: DashboardSync()
    object DisableSync: DashboardSync()
    object CheckSync: DashboardSync()
}