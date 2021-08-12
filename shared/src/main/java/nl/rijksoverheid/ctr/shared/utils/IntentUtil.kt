package nl.rijksoverheid.ctr.shared.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface IntentUtil {

    fun openPlayStore(context: Context)
}

class IntentUtilImpl() : IntentUtil {

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

