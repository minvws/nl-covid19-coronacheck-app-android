package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface ReturnToAppUseCase {
    fun get(uri: String): Intent?
}

class ReturnToAppUseCaseImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ReturnToAppUseCase {

    override fun get(uri: String): Intent? {
        return if (isWhitelisted(uri)) {
            Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
        } else null
    }

    private fun isWhitelisted(uri: String) =
        cachedAppConfigUseCase.getCachedAppConfig().returnApps.any { uri.contains(it.code) }
}
