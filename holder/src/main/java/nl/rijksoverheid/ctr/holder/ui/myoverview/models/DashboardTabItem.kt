package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem

@Parcelize
data class DashboardTabItem(
    @StringRes val title: Int,
    val greenCardType: GreenCardType,
    val items: List<MyOverviewItem>
): Parcelable