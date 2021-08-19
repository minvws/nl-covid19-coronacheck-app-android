package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ExternalReturnAppData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ReturnToExternalAppUseCase {

    /**
     * Determine whether the given uri from an external app is valid to return to
     *
     * @param[uri] the uri given by an external app when this app is entered through deep link.
     * @param[type] Type of green card: domestic or international
     * @return Return app data with app name and intent or null when given uri is illegal.
     *
     */
    fun get(uri: String, type: GreenCardType): ExternalReturnAppData?
}

class ReturnToExternalAppUseCaseImpl(
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : ReturnToExternalAppUseCase {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun get(uri: String, type: GreenCardType): ExternalReturnAppData? {
        return holderConfig.deeplinkDomains
            .takeIf { type == GreenCardType.Domestic }
            ?.firstOrNull { uri.contains("https://${it.url}") }
            ?.let {
                ExternalReturnAppData(
                    appName = it.name,
                    intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
                )
            }
    }
}
