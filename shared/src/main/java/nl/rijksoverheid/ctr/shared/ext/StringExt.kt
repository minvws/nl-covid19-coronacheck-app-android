package nl.rijksoverheid.ctr.shared.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.browser.customtabs.CustomTabsIntent
import com.squareup.moshi.Moshi

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

fun String.launchUrl(context: Context, noBrowserBlock: () -> Unit = {}) {
    try {
        CustomTabsIntent.Builder().build().also {
            it.launchUrl(context, Uri.parse(this))
        }
    } catch (exception: ActivityNotFoundException) {
        // if chrome app is disabled or not there, try an alternative
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(this)))
        } catch (exception: ActivityNotFoundException) {
            noBrowserBlock()
        }
    }
}

fun String.removeWhitespace() : String {
    return this.replace("\\s+".toRegex(), "")
}
