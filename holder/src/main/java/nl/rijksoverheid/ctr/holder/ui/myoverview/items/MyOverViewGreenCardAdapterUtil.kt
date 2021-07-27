package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState

interface MyOverViewGreenCardAdapterUtil {
    fun setContent()
}

class MyOverViewGreenCardAdapterUtilImpl(
    private val context: Context,
    private val greenCard: GreenCard,
    private val originStates: List<OriginState>,
    private val viewBinding: ItemMyOverviewGreenCardBinding
): MyOverViewGreenCardAdapterUtil {
    override fun setContent() {

    }
}