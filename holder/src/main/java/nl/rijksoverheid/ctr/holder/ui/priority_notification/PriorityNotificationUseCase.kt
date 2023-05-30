package nl.rijksoverheid.ctr.holder.ui.priority_notification

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigUseCase
import nl.rijksoverheid.ctr.shared.ext.toObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface PriorityNotificationUseCase {
    suspend fun get(): String?
}

class PriorityNotificationUseCaseImpl(
    private val appConfigUseCase: AppConfigUseCase,
    private val moshi: Moshi
) : PriorityNotificationUseCase {
    override suspend fun get(): String? {
        val configResult = appConfigUseCase.get()

        if (configResult is ConfigResult.Success) {
            return try {
                val config = configResult.appConfig.toObject<HolderConfig>(moshi)
                config.priorityNotification
            } catch (exception: Exception) {
                null
            }
        }

        return null
    }
}
