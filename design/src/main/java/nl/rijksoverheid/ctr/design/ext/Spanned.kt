package nl.rijksoverheid.ctr.design.ext

import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.RelativeSizeSpan
import androidx.core.text.getSpans

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

    while (true) {
        val index = indexOf(separator, start)

        if (index == -1) {
            break
        }

        (subSequence(start, index) as? Spanned)?.let { substring ->
            substrings.add(substring)
        }

        start = index + separator.length
    }

    return substrings
}

val Spanned.isHeading: Boolean
    get() {
        return getSpans<RelativeSizeSpan>().any { span ->
            span.sizeChange > 0
        }
    }

val Spanned.isListItem: Boolean
    get() {
        return getSpans<BulletSpan>().isNotEmpty()
    }