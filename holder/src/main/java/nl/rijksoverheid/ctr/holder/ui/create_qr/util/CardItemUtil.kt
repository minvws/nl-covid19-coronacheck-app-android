/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy

interface CardItemUtil {
    fun getDisclosurePolicy(
        greenCard: GreenCard
    ): GreenCardDisclosurePolicy

    fun getEnabledState(
        greenCard: GreenCard
    ): GreenCardEnabledState
}

class CardItemUtilImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val greenCardUtil: GreenCardUtil
): CardItemUtil {

    override fun getDisclosurePolicy(
        greenCard: GreenCard
    ): GreenCardDisclosurePolicy {
        return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                when (featureFlagUseCase.getDisclosurePolicy()) {
                    is DisclosurePolicy.OneG -> {
                        GreenCardDisclosurePolicy.OneG
                    }
                    is DisclosurePolicy.ThreeG -> {
                        GreenCardDisclosurePolicy.ThreeG
                    }
                    is DisclosurePolicy.OneAndThreeG -> {
                        // TODO Return OneG based on credential category
                        GreenCardDisclosurePolicy.ThreeG
                    }
                }
            }
            is GreenCardType.Eu -> {
                GreenCardDisclosurePolicy.ThreeG
            }
        }
    }

    override fun getEnabledState(
        greenCard: GreenCard
    ): GreenCardEnabledState {
        return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                if (featureFlagUseCase.getDisclosurePolicy() == DisclosurePolicy.OneG &&
                    !greenCardUtil.hasOrigin(listOf(greenCard), OriginType.Test) ) {
                    GreenCardEnabledState.Disabled()
                } else {
                    GreenCardEnabledState.Enabled
                }
            }
            is GreenCardType.Eu -> {
                GreenCardEnabledState.Enabled
            }
        }
    }
}