package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.utils.Accessibility

class HtmlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
) : MaterialTextView(context, attrs, defStyle, defStyleRes) {

    init {
        TextViewCompat.setTextAppearance(this, R.style.App_TextAppearance_MaterialComponents_Body1)
    }

    // Add support for activating links when using touch exploration
    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        // Check if eventType is TYPE_VIEW_CLICKED
        if (event == null || event.eventType != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return super.dispatchPopulateAccessibilityEvent(event)
        }

        // Check if touch exploration is enabled (e.g. TalkBack)
        if (!Accessibility.touchExploration(context)) {
            return super.dispatchPopulateAccessibilityEvent(event)
        }

        // Try to get text as Spanned object
        (text as? Spanned)?.let { spanned ->
            // Extract all ClickableSpan instances
            val clickableSpans = spanned.getSpans(0, spanned.length, ClickableSpan::class.java)

            // Activate the first clickable span, if it exists
            clickableSpans.firstOrNull()?.onClick(this)
        }

        return super.dispatchPopulateAccessibilityEvent(event)
    }
}
