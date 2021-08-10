package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ReturnAppData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ReturnToAppUseCase {
    fun get(uri: String, type: GreenCardType): ReturnAppData?
}

class ReturnToAppUseCaseImpl(
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : ReturnToAppUseCase {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun get(uri: String, type: GreenCardType): ReturnAppData? {
        return holderConfig.deeplinkDomains
            .takeIf { type == GreenCardType.Domestic }
            ?.firstOrNull { uri.contains("https://${it.url}") }
            ?.let {
                ReturnAppData(
                    appName = it.name,
                    intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
                )
            }
    }
}
