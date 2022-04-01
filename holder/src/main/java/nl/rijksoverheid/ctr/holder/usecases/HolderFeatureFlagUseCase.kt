/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.usecases

import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface HolderFeatureFlagUseCase {
    fun getDisclosurePolicy(): DisclosurePolicy
}

class HolderFeatureFlagUseCaseImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase
): HolderFeatureFlagUseCase {

    override fun getDisclosurePolicy(): DisclosurePolicy {
        return cachedAppConfigUseCase.getCachedAppConfig().disclosurePolicy
    }
}