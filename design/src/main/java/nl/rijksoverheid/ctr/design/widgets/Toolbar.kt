package nl.rijksoverheid.ctr.design.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.ActionMenuView
import androidx.core.view.children
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.utils.Accessibility

/**
 * Adds accessibility markup to the Toolbar
 */
class Toolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : androidx.appcompat.widget.Toolbar(context, attrs, defStyleAttr) {

    override fun inflateMenu(resId: Int) {
        super.inflateMenu(resId)

        // Mark menu items as button
        children.filterIsInstance(ActionMenuView::class.java)
            .firstOrNull()?.let { menu ->
                menu.children.forEach { view ->
                    Accessibility.button(view, true)
                }
            }
    }
}
