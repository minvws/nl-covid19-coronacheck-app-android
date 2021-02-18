package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import com.squareup.moshi.Moshi
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
inline fun <reified O> String.toObject(moshi: Moshi): O {
    return moshi.adapter(O::class.java).fromJson(this)
        ?: throw Exception("Failed to create object from json string")
}

fun String.fromHtml(): Spannable {
    // marker object. We can't directly use BulletSpan as this crashes on Android 6
    class Bullet

    val htmlSpannable = HtmlCompat.fromHtml(
        this,
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
            BulletSpan(100, Color.RED),
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
    return spannableBuilder
}

fun String.launchUrl(context: Context) {
    CustomTabsIntent.Builder().build().also {
        it.launchUrl(context, Uri.parse(this))
    }
}
