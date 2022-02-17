/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface ShowDisclosurePolicyUseCase {
    fun get(): DisclosurePolicy?
}

class ShowDisclosurePolicyUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val persistenceManager: PersistenceManager
) : ShowDisclosurePolicyUseCase {

    override fun get(): DisclosurePolicy? {
        val currentPolicy = featureFlagUseCase.getDisclosurePolicy()
        return if (currentPolicy != persistenceManager.getPolicyScreenSeen()) currentPolicy else null
    }
}