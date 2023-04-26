package nl.rijksoverheid.ctr.design.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEachIndexed
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.ViewPagerIndicatorBubbleBinding
import nl.rijksoverheid.ctr.shared.utils.Accessibility

class ViewPagerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var selectedIndex = 0

    init {
        orientation = HORIZONTAL
        // allow the indicator only for Talkback users
        if (Accessibility.screenReader(context)) {
            isFocusable = true
        }
    }

    fun initIndicator(amount: Int) {
        if (amount > 1) {
            repeat(amount) {
                addView(ViewPagerIndicatorBubble(context))
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun updateSelected(selectedIndex: Int) {
        forEachIndexed { index, view ->
            (view as ViewPagerIndicatorBubble).toggleSelected(index == selectedIndex)
        }
        contentDescription = context.getString(R.string.page_indicator_label, selectedIndex + 1, childCount)
        this.selectedIndex = selectedIndex
    }

    private class ViewPagerIndicatorBubble @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : ConstraintLayout(context, attrs, defStyleAttr) {

        val binding: ViewPagerIndicatorBubbleBinding =
            ViewPagerIndicatorBubbleBinding.inflate(LayoutInflater.from(context), this)

        init {
            val padding =
                resources.getDimensionPixelSize(R.dimen.view_pager_indicator_bubble_spacing)
            setPadding(padding, padding, padding, padding)
        }

        fun toggleSelected(isSelected: Boolean) {
            binding.selected.visibility = if (isSelected) VISIBLE else INVISIBLE
            binding.unselected.visibility = if (isSelected) INVISIBLE else VISIBLE
        }
    }
}
