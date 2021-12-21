/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager

interface CheckNewValidityInfoCardUseCase {
    suspend fun check()
}

class CheckNewValidityInfoCardUseCaseImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager): CheckNewValidityInfoCardUseCase {

    override suspend fun check() {
        val showNewValidityInfoCard = cachedAppConfigUseCase.getCachedAppConfig().showNewValidityInfoCard
        val checkNewValidityInfoCard = persistenceManager.getCheckNewValidityInfoCard()

        if (showNewValidityInfoCard && checkNewValidityInfoCard) {
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(false)
            persistenceManager.setHasDismissedNewValidityInfoCard(false)
        }
    }
}