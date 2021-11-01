package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.view.ViewCompat
import androidx.core.view.children
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.ext.*
import nl.rijksoverheid.ctr.design.spans.BulletPointSpan

/**
 * The HtmlTextViewWidget is able to display (simple) HTML in an accessible way.
 * 1. HTML is parsed to a Spanned object.
 * 2. the Spanned object is split on the linebreak character.
 * 3. Each Spanned object is then displayed using a `HtmlTextView`.
 * 4. Accessibility attributes are applied to the HtmlTextView.
 *
 * The methods enableHtmlLinks() and enableCustomLinks() are dispatched to each TextView subview.
 * The getSpannableFromHtml() method parses HTML into a Spannable object while taking legacy implementations into account.
 */
class HtmlTextViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private val HTML_LINKS_ENABLED = false
    private val PARAGRAPH_MARGIN_MULTIPLIER = 1.0f
    private val HEADING_MARGIN_MULTIPLIER = 1.0f
    private val LIST_ITEM_MARGIN_MULTIPLIER = 0.25f

    private val textColorPrimary by lazy {
        context.getAttrColor(android.R.attr.textColorPrimary)
    }

    private val textColorLink by lazy {
        context.getAttrColor(android.R.attr.textColorLink)
    }

    // Reflects the unparsed HTML text shown in the subviews. Can only be set internally.
    var text: String? = null
        private set

    // Reflects the parsed HTML text shown in the subviews. Can only be set internally.
    var spannable: Spannable? = null
        private set

    /**
     * Checks if any attributes were passed by XML, and if so, calls the relevant methods.
     */
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HtmlTextViewWidget,
            defStyle,
            defStyleRes
        ).apply {
            try {
                val htmlText = getText(R.styleable.HtmlTextViewWidget_htmlText)
                if (htmlText?.isNotEmpty() == true) {
                    setHtmlText(
                        htmlText = htmlText.toString(),
                        htmlTextColor = getColor(R.styleable.HtmlTextViewWidget_htmlTextColor, textColorPrimary),
                        htmlTextColorLink = getColor(R.styleable.HtmlTextViewWidget_htmlTextColorLink, textColorLink),
                        htmlLinksEnabled = getBoolean(R.styleable.HtmlTextViewWidget_enableHtmlLinks, HTML_LINKS_ENABLED),
                        paragraphMarginMultiplier = getFloat(R.styleable.HtmlTextViewWidget_enableHtmlLinks, PARAGRAPH_MARGIN_MULTIPLIER),
                        headingMarginMultiplier = getFloat(R.styleable.HtmlTextViewWidget_enableHtmlLinks, HEADING_MARGIN_MULTIPLIER),
                        listItemMarginMultiplier = getFloat(R.styleable.HtmlTextViewWidget_enableHtmlLinks, LIST_ITEM_MARGIN_MULTIPLIER),
                    )
                }
            } finally {
                recycle()
            }
        }
        orientation = VERTICAL
    }

    /**
     * Sets the text based on a string resource.
     * Links are disabled by default, but can be enabled.
     */
    fun setHtmlText(htmlText: Int, htmlLinksEnabled: Boolean = false) {
        val text = context.getString(htmlText)
        setHtmlText(text, htmlLinksEnabled = htmlLinksEnabled)
    }

    /**
     * Sets the text based on a string.
     * Links are disabled by default, but can be enabled.
     * A multiplier can be set for the paragraph, heading and list item margins.
     */
    fun setHtmlText(
        htmlText: String,
        htmlLinksEnabled: Boolean = HTML_LINKS_ENABLED,
        htmlTextColor: Int = textColorPrimary,
        htmlTextColorLink: Int = textColorLink,
        paragraphMarginMultiplier: Float = PARAGRAPH_MARGIN_MULTIPLIER,
        headingMarginMultiplier: Float = HEADING_MARGIN_MULTIPLIER,
        listItemMarginMultiplier: Float = LIST_ITEM_MARGIN_MULTIPLIER
    ) {
        removeAllViews()

        if (htmlText.isEmpty()) {
            return
        }
        this.text = htmlText

        // Step 1: Parse the given String into a Spannable
        val spannable = getSpannableFromHtml(htmlText)
        this.spannable = spannable

        // Step 2: Separate the Spannable on each linebreak
        val parts = listOf(spannable) // spannable.separated("\n") --> NOTE: disabled for now

        // Step 3: Add a HtmlTextView for each part of the Spannable
        parts.forEachIndexed { index, part ->
            val textView = HtmlTextView(context)
            textView.setTextColor(htmlTextColor)
            textView.setLinkTextColor(htmlTextColorLink)
            textView.text = part

            // Mark as heading?
            if (part.isHeading) {
                ViewCompat.setAccessibilityHeading(textView, true)
            }

            // Hide for assistive technologies?
            if (part.isBlank()) {
                textView.isFocusable = false
                textView.isFocusableInTouchMode = false
                textView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }

            addView(textView)
        }

        // Step 4: Enable links if requested
        if (htmlLinksEnabled) {
            enableHtmlLinks()
        }
    }

    /**
     * Enables HTML links for all of it's TextView subviews
     */
    fun enableHtmlLinks() {
        children.filterIsInstance(TextView::class.java).forEach { textView ->
            textView.enableHtmlLinks()
        }
    }

    /**
     * Enables custom links for all of it's TextView subviews
     */
    fun enableCustomLinks(onLinkClick: () -> Unit) {
        children.filterIsInstance(TextView::class.java).forEach { textView ->
            textView.enableCustomLinks(onLinkClick)
        }
    }

    /**
     * Parses a String into a Spannable object.
     * It takes both legacy and modern HTML parsing implementations into account.
     */
    private fun getSpannableFromHtml(html: String): Spannable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getSpannableFromHtmlModern(html)
        } else {
            getSpannableFromHtmlLegacy(html)
        }
    }

    /**
     * Parses a String into a Spannable object.
     * It then replaces all BulletSpan's with BulletPointSpans for consistent styling.
     */
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

    /**
     * Parses a String into a Spannable object.
     * During parsing, BulletSpans are replaced with a temporary Bullet marker.
     * This is done because using BulletSpan directly can cause crashes on Android 6.
     * After parsing, all Bullets are replaced with BulletSpans.
     */
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
}
