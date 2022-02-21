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

interface ShowNewDisclosurePolicyUseCase {

    /**
     * Get the disclosure policy when it has been changed from the config
     *
     * @return The new disclosure policy if it's new or null if the policy hasn't changed.
     */
    fun get(): DisclosurePolicy?
}

class ShowNewDisclosurePolicyUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val persistenceManager: PersistenceManager
) : ShowNewDisclosurePolicyUseCase {

    override fun get(): DisclosurePolicy? {
        val currentPolicy = featureFlagUseCase.getDisclosurePolicy()
        return if (currentPolicy != persistenceManager.getPolicyScreenSeen()) currentPolicy else null
    }
}