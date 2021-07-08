package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import nl.rijksoverheid.ctr.shared.utils.Accessibility

/**
 * Marks a CardView as a button for accessibility
 */
class CardViewButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    init {
        Accessibility.button(this)
    }
}