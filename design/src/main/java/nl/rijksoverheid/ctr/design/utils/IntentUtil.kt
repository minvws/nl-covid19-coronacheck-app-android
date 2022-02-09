/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.design.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.shared.ext.launchUrl

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface IntentUtil {
    fun openUrl(context: Context, url: String)
    fun openPlayStore(context: Context)
}

class IntentUtilImpl(private val dialogUtil: DialogUtil) : IntentUtil {
    @SuppressLint("StringFormatInvalid")
    override fun openUrl(context: Context, url: String) {
        url.launchUrl(context) {
            dialogUtil.presentDialog(
                context = context,
                title = R.string.dialog_no_browser_title,
                // remove the https prefix to make it more eye friendsly
                message = context.getString(R.string.dialog_no_browser_message, url).replace(
                    "https://",
                    ""
                ),
                positiveButtonText = R.string.ok,
                positiveButtonCallback = {},
            )
        }
    }

    override fun openPlayStore(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${context.packageName}")
        ).setPackage("com.android.vending")
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // fall back to browser intent
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                )
            )
        }
    }
}

