package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import android.content.Intent
import android.net.Uri
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ReturnAppData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ReturnToAppUseCase {
    fun get(uri: String): ReturnAppData?
}

class ReturnToAppUseCaseImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ReturnToAppUseCase {

    override fun get(uri: String): ReturnAppData? {
        return (cachedAppConfigUseCase.getCachedAppConfig() as HolderConfig).returnApps
            .firstOrNull { uri.contains(it.url) }
            ?.let {
                ReturnAppData(
                    appName = it.name,
                    intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
                )
            }
    }
}
