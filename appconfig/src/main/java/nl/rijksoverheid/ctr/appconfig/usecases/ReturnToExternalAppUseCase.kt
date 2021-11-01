/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.appconfig.models.ExternalReturnAppData

interface ReturnToExternalAppUseCase {

    /**
     * Determine whether the given uri from an external app is valid to return to
     *
     * @param[uri] the uri given by an external app when this app is entered through deep link.
     * @return Return app data with app name and intent or null when given uri is illegal.
     *
     */
    fun get(uri: String): ExternalReturnAppData?
}

class ReturnToExternalAppUseCaseImpl(
    val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ReturnToExternalAppUseCase {


    override fun get(uri: String): ExternalReturnAppData? {
        return cachedAppConfigUseCase.getCachedAppConfig().deeplinkDomains
            .firstOrNull { uri.contains("https://${it.url}") }
            ?.let {
                ExternalReturnAppData(
                    appName = it.name,
                    intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
                )
            }
    }
}
