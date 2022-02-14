/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

/**
 * Remove expired green cards (= no more origins). We do this every time before fetching green cards.
 * The reason for this is that we transform the database model in [SplitDomesticGreenCardsUseCase].
 * If there is one green card with three origins (vaccination, recovery, test), there are cases we split it
 * into two green card (green card 1: vaccination, recovery and green card 2: test) in [SplitDomesticGreenCardsUseCase]
 * In case the origin of the second "fake" green card expires, we don't want to remove the green card but only the origin.
 */
interface RemoveExpiredGreenCardsUseCase {
    suspend fun execute(allGreenCards: List<GreenCard>)
}

class RemoveExpiredGreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase
): RemoveExpiredGreenCardsUseCase {

    override suspend fun execute(allGreenCards: List<GreenCard>) {
        val greenCardsToRemove = allGreenCards
            .filter {
                it.origins.isEmpty()
            }

        greenCardsToRemove.forEach {
            holderDatabase.greenCardDao().delete(it.greenCardEntity)
        }
    }
}