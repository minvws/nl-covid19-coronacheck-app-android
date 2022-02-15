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

interface NewDisclosurePolicySeenUseCase {
    fun get(): Boolean
}

class NewDisclosurePolicySeenUseCaseImpl(
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val persistenceManager: PersistenceManager
): NewDisclosurePolicySeenUseCase {

    override fun get(): Boolean {
        return featureFlagUseCase.getDisclosurePolicy() != persistenceManager.getPolicyScreenSeen()
    }
}