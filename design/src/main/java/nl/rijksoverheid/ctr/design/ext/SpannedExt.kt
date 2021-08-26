package nl.rijksoverheid.ctr.design.ext

import android.graphics.Typeface
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

/**
 * Separates a Spanned object using the given separator into a list of Spanned instances
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

/**
 * Extracts a substring of the given Spanned object
 * The subSequence method should never fail, but if it does, it returns the original Spanned object
 */
fun Spanned.substring(start: Int, end: Int): Spanned {
    (subSequence(start, end) as? Spanned)?.let { substring ->
        return substring
    }
    return this
}

/**
 * Determines whether a Spanned object is a heading
 * This is the case in two situations:
 * (1) If the Spanned object contains a StyleSpan with Typeface.Bold for the whole length
 * (2) If the Spanned object contains one or more RelativeSizeSpans with a size change (= h1-h6)
 */
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

/**
 * Determines whether a Spanned object is a list item
 * This is the case if the Spanned object contains a BulletSpan or BulletPointSpan
 */
val Spanned.isListItem: Boolean
    get() {
        return getSpans<BulletSpan>().isNotEmpty() ||
               getSpans<BulletPointSpan>().isNotEmpty()
    }