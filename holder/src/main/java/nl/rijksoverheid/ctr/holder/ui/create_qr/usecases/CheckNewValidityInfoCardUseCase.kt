/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

interface CheckNewValidityInfoCardUseCase {
    suspend fun check()
}

class CheckNewValidityInfoCardUseCaseImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
    private val holderDatabase: HolderDatabase): CheckNewValidityInfoCardUseCase {

    override suspend fun check() {
        val showNewValidityInfoCard = cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard
        val checkNewValidityInfoCard = persistenceManager.getCheckNewValidityInfoCard()
        val hasDomesticVaccinationOrRecovery = holderDatabase.greenCardDao().getAll()
            .filter { it.greenCardEntity.type is GreenCardType.Domestic }
            .any { it.origins.any { origin -> origin.type is OriginType.Vaccination || origin.type is OriginType.Recovery } }

        if (showNewValidityInfoCard && checkNewValidityInfoCard) {
            if (hasDomesticVaccinationOrRecovery) {
                persistenceManager.setHasDismissedNewValidityInfoCard(false)
            }

            // Only execute this check once
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(false)
        }
    }
}