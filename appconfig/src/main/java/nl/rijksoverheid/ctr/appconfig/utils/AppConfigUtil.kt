package nl.rijksoverheid.ctr.appconfig

import android.content.Context
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AppConfigUtil {
    fun getStringWithTestValidity(@StringRes text: Int): String
}

class AppConfigUtilImpl(
    private val context: Context,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : AppConfigUtil {
    override fun getStringWithTestValidity(@StringRes text: Int): String {
        return context.getString(
            text,
            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toString()
        )
    }
}
