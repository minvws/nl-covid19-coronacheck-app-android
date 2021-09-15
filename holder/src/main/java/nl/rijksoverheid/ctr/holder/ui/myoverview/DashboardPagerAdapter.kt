package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem

/**
 * viewpager adapter to house green card overviews for domestic and European.
 *
 * @param[fragment] Tabs fragment with viewpager where the overviews are nested within.
 * @param[returnToExternalAppUri] Uri used to return to external app from which it was deep linked from.
 */
class DashboardPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val returnToExternalAppUri: String?) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val items: List<DashboardTabItem> = mutableListOf()

    fun setItems(items: List<DashboardTabItem>) {
        (this.items as MutableList<DashboardTabItem>).addAll(items)
        notifyItemRangeInserted(0, items.size)
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return MyOverviewFragment.getInstance(
            greenCardType = items[position].greenCardType,
            returnUri = returnToExternalAppUri
        )
    }
}