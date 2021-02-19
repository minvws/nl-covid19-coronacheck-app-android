package nl.rijksoverheid.ctr.shared.util

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import nl.rijksoverheid.ctr.shared.R
import nl.rijksoverheid.ctr.shared.spans.BulletPointSpan

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

// SEE: https://medium.com/ackee/how-to-make-bulletproof-bullet-lists-in-textview-223c54fb21e6
fun getSpannableFromHtml(context: Context, html: String): Spannable {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val spannableBuilder = getSpannableFromHtmlLegacy(context, html)
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
        return getSpannableFromHtmlLegacy(context, html)
    }
}

private fun getSpannableFromHtmlLegacy(context: Context, html: String): Spannable {
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
