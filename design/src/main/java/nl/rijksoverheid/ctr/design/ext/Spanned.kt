package nl.rijksoverheid.ctr.design.ext

import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import nl.rijksoverheid.ctr.design.spans.BulletPointSpan

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun Spanned.separated(separator: String): List<Spanned> {
    val substrings = arrayListOf<Spanned>()

    var start = 0
    var index = indexOf(separator, start)

    while (index != -1) {
        substrings.add(substring(start, index))

        start = index + separator.length
        index = indexOf(separator, start)
    }

    substrings.add(substring(start, length))

    return substrings
}

fun Spanned.substring(start: Int, end: Int): Spanned {
    (subSequence(start, end) as? Spanned)?.let { substring ->
        return substring
    }
    return this
}

val Spanned.isHeading: Boolean
    get() {
        return getSpans<StyleSpan>().any { span ->
            return span.style == Typeface.BOLD &&
                   getSpanStart(span) == 0 &&
                   getSpanEnd(span) == length
        } || getSpans<RelativeSizeSpan>().any { span ->
            return span.sizeChange > 0
        }
    }

val Spanned.isListItem: Boolean
    get() {
        return getSpans<BulletSpan>().isNotEmpty() ||
               getSpans<BulletPointSpan>().isNotEmpty()
    }