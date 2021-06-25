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
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.text.parseAsHtml
import com.google.android.material.textview.MaterialTextView
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.ext.enableHtmlLinks
import nl.rijksoverheid.ctr.design.spans.BulletPointSpan
import nl.rijksoverheid.ctr.shared.utils.Accessibility

class HtmlTextViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : MaterialTextView(context, attrs, defStyle, defStyleRes) {

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HtmlTextViewWidget,
            0, 0
        ).apply {
            try {
                val htmlText =
                    getText(R.styleable.HtmlTextViewWidget_htmlText)
                if (htmlText?.isNotEmpty() == true) {
                    setHtmlText(
                        htmlText.toString(),
                        getBoolean(R.styleable.HtmlTextViewWidget_enableHtmlLinks, false)
                    )
                    text = htmlText.toString().parseAsHtml()
                }

                val htmlTextWithBullets = getText(R.styleable.HtmlTextViewWidget_htmlTextWithList)
                if (htmlTextWithBullets?.isNotEmpty() == true) {
                    setHtmlTextWithBullets(
                        htmlTextWithBullets.toString(),
                        getBoolean(R.styleable.HtmlTextViewWidget_enableHtmlLinks, false)
                    )

                }
            } finally {
                recycle()
            }
        }
    }

    fun setHtmlText(htmlText: String, htmlLinksEnabled: Boolean = false) {
        text = htmlText.parseAsHtml()
        if (htmlLinksEnabled) {
            enableHtmlLinks()
        }
    }

    fun setHtmlTextWithBullets(htmlText: String, htmlLinksEnabled: Boolean = false) {
        text = getSpannableFromHtml(htmlText)
        if (htmlLinksEnabled) {
            enableHtmlLinks()
        }
    }

    private fun getSpannableFromHtml(html: String): Spannable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val spannableBuilder = getSpannableFromHtmlLegacy(html)
            val bulletSpans =
                spannableBuilder.getSpans<BulletSpan>()

            bulletSpans.forEach {
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
        } else {
            return getSpannableFromHtmlLegacy(html)
        }
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
