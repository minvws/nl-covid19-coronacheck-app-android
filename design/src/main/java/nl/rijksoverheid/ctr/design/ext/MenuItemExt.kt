package nl.rijksoverheid.ctr.design.ext

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.MenuItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun MenuItem.styleTitle(context: Context, appearance: Int) {
    val title = this.title
    val outValue = TypedValue()
    context.theme.resolveAttribute(
        appearance,
        outValue,
        true
    )
    val spannableStringBuilder = SpannableStringBuilder(title)
    spannableStringBuilder.setSpan(
        TextAppearanceSpan(
            context, outValue.resourceId
        ),
        0,
        title.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    this.title = spannableStringBuilder
}
