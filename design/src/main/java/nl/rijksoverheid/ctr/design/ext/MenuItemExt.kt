package nl.rijksoverheid.ctr.design.ext

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.MenuItem
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.utils.CustomTypefaceSpan

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun MenuItem.styleTitle(context: Context, appearance: Int, heading: Boolean = false) {
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

    // Android versions O and earlier have issues setting the font directly through the style
    // so we're setting it manually if needed
    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
        val fontToUse = if (heading) {
            R.font.montserrat_bold
        } else {
            R.font.montserrat_semibold
        }
        spannableStringBuilder.setSpan(
            // CustomTypefaceSpan used to set typeface as using the family doesn't work on M
            // and supplying a typeface directly requires API 28
            CustomTypefaceSpan(ResourcesCompat.getFont(context, fontToUse)),
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    this.title = spannableStringBuilder
}
