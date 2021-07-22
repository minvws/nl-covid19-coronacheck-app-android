package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.ext.*
import nl.rijksoverheid.ctr.design.spans.BulletPointSpan
import nl.rijksoverheid.ctr.shared.utils.Accessibility

class HtmlTextViewWidget @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyle: Int = 0,
    private val defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    var text: CharSequence? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HtmlTextViewWidget,
            defStyle,
            defStyleRes
        ).apply {
            try {
                val htmlText =
                    getText(R.styleable.HtmlTextViewWidget_htmlText)
                if (htmlText?.isNotEmpty() == true) {
                    setHtmlText(
                        htmlText.toString(),
                        getBoolean(R.styleable.HtmlTextViewWidget_enableHtmlLinks, false)
                    )
                }
            } finally {
                recycle()
            }
        }
        orientation = VERTICAL
    }

    fun setHtmlText(htmlText: Int, htmlLinksEnabled: Boolean = false) {
        val text = context.getString(htmlText)
        setHtmlText(text, htmlLinksEnabled)
    }

    fun setHtmlText(htmlText: String?, htmlLinksEnabled: Boolean = false) {
        if (htmlText == null) {
            text = ""
            return
        }

        val spannable = getSpannableFromHtml(htmlText)
        text = spannable

        removeAllViews()

        val parts = spannable.separated("\n")
        val iterator = parts.iterator()
        while (iterator.hasNext()) {
            val part = iterator.next()

            val textView = MaterialTextView(context, attrs, defStyle, defStyleRes)
            TextViewCompat.setTextAppearance(textView, R.style.App_TextAppearance_MaterialComponents_Body1)
            textView.text = part

            if (part.isHeading) {
                ViewCompat.setAccessibilityHeading(textView, true)
            }

            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            if (iterator.hasNext()) {
                val marginBottom = if (part.isHeading || part.isListItem) {
                    textView.lineHeight / 4
                } else {
                    textView.lineHeight
                }
                params.setMargins(0, 0, 0, marginBottom)
            }
            textView.layoutParams = params

            addView(textView)
        }

        if (htmlLinksEnabled) {
            enableHtmlLinks()
        }
    }

    fun enableHtmlLinks() {
        children.filterIsInstance(TextView::class.java).forEach { textView ->
            textView.enableHtmlLinks()
        }
    }

    fun enableCustomLinks(onLinkClick: () -> Unit) {
        children.filterIsInstance(TextView::class.java).forEach { textView ->
            textView.enableCustomLinks(onLinkClick)
        }
    }

    private fun getSpannableFromHtml(html: String): Spannable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getSpannableFromHtmlModern(html)
        } else {
            getSpannableFromHtmlLegacy(html)
        }
    }

    private fun getSpannableFromHtmlModern(html: String): Spannable {
        val spannableBuilder = getSpannableFromHtmlLegacy(html)

        spannableBuilder.getSpans<BulletSpan>().forEach {
            val start = spannableBuilder.getSpanStart(it)
            val end = spannableBuilder.getSpanEnd(it)
            spannableBuilder.removeSpan(it)
            spannableBuilder.setSpan(
                BulletPointSpan(context.resources.getDimensionPixelSize(R.dimen.bullet_point_span_gap_width)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableBuilder
    }

    private fun getSpannableFromHtmlLegacy(html: String): Spannable {
        // marker object. We can't directly use BulletSpan as this crashes on Android 6
        class Bullet

        val htmlSpannable = HtmlCompat.fromHtml(
            html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            null,
            { opening, tag, output, _ ->
                if (tag == "li" && opening) {
                    output.setSpan(
                        Bullet(),
                        output.length,
                        output.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                if (tag == "ul" && opening) {
                    // add a line break if this tag is not on a new line
                    if (output.isNotEmpty()) {
                        output.append("\n")
                    }
                }
                if (tag == "li" && !opening) {
                    output.append("\n")
                    val lastMark =
                        output.getSpans<Bullet>().lastOrNull()
                    lastMark?.let {
                        val start = output.getSpanStart(it)
                        output.removeSpan(it)
                        if (start != output.length) {
                            output.setSpan(
                                it,
                                start,
                                output.length,
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }
        )

        val spannableBuilder = SpannableStringBuilder(htmlSpannable)
        // replace the marker with BulletSpan if the markers have been added
        spannableBuilder.getSpans<Bullet>().forEach {
            val start = spannableBuilder.getSpanStart(it)
            val end = spannableBuilder.getSpanEnd(it)
            spannableBuilder.removeSpan(it)
            spannableBuilder.setSpan(
                BulletSpan(
                    context.resources.getDimensionPixelSize(R.dimen.bullet_point_span_gap_width)
                ),
                start,
                end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        return spannableBuilder
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
