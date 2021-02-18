package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import androidx.browser.customtabs.CustomTabsIntent
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.util.getSpannableFromHtml

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

inline fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

inline fun String.fromHtmlWithStyling(context: Context): Spannable {
    return getSpannableFromHtml(context, this)
}

fun String.launchUrl(context: Context) {
    CustomTabsIntent.Builder().build().also {
        it.launchUrl(context, Uri.parse(this))
    }
}
