/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface SplitDomesticGreenCardsUseCase {

    /**
     * Split the domestic green cards based on the [DisclosurePolicy] that is set
     * @param domesticGreenCards The locally stored domestic green cards
     * @return The splitted domestic green cards to present
     */
    fun getSplittedDomesticGreenCards(domesticGreenCards: List<GreenCard>): List<GreenCard>
}

class SplitDomesticGreenCardsUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
): SplitDomesticGreenCardsUseCase {

    override fun getSplittedDomesticGreenCards(domesticGreenCards: List<GreenCard>): List<GreenCard> {
        return when (featureFlagUseCase.getDisclosurePolicy()) {
            is DisclosurePolicy.OneG -> splitTestDomesticGreenCard(domesticGreenCards)
            is DisclosurePolicy.OneAndThreeG -> splitTestDomesticGreenCard(domesticGreenCards)
            is DisclosurePolicy.ThreeG -> domesticGreenCards
        }
    }

    private fun splitTestDomesticGreenCard(domesticGreenCards: List<GreenCard>): List<GreenCard> {
        val hasTestOrigin = greenCardUtil.hasOrigin(
            greenCards = domesticGreenCards,
            originType = OriginType.Test
        )

        return if (hasTestOrigin) {
            domesticGreenCards
                .map {
                    it.copy(
                        origins = it.origins.filter { origin -> origin.type !is OriginType.Test }
                    )
                }
                .toMutableList()
                .also { greenCards ->
                    greenCards.add(
                        greenCards.first().copy(
                            origins = domesticGreenCards.first().origins.filter { origin -> origin.type is OriginType.Test }
                        )
                    )
                }
        } else {
            domesticGreenCards
        }
    }
}