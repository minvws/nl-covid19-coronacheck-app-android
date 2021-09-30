package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem

data class DashboardTabItem(
    @StringRes val title: Int,
    val greenCardType: GreenCardType,
    val items: List<DashboardItem>
)