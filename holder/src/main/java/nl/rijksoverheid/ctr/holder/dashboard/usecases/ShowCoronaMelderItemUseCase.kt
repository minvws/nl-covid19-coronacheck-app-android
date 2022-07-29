package nl.rijksoverheid.ctr.holder.dashboard.usecases

import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
interface ShowCoronaMelderItemUseCase {
    fun shouldShowCoronaMelderItem(
        greenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult
    ): Boolean
}

class ShowCoronaMelderItemUseCaseImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil
) : ShowCoronaMelderItemUseCase {
    override fun shouldShowCoronaMelderItem(
        greenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult
    ): Boolean {
        val appConfig = cachedAppConfigUseCase.getCachedAppConfig()
        val shouldShowCoronaMelderRecommendation =
            appConfig.shouldShowCoronaMelderRecommendation ?: false

        return shouldShowCoronaMelderRecommendation &&
                greenCards.isNotEmpty() &&
                !greenCards.all {
                    greenCardUtil.isExpired(
                        it
                    )
                } &&
                databaseSyncerResult is DatabaseSyncerResult.Success
    }
}
