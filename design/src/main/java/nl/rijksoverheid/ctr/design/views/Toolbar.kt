package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
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

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)

        // Mark title as heading
        children.filterIsInstance(TextView::class.java)
                .firstOrNull()?.let { textView ->
            Accessibility.heading(textView, true)
        }
    }

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