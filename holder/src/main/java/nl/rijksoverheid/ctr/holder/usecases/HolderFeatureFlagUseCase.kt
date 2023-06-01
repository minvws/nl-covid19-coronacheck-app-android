/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.usecases

import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase

interface HolderFeatureFlagUseCase {
    fun getGgdEnabled(): Boolean
    fun getMijnCnEnabled(): Boolean
    fun getPapEnabled(): Boolean
}

class HolderFeatureFlagUseCaseImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase
) : HolderFeatureFlagUseCase {

    override fun getGgdEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().ggdEnabled
    }

    override fun getMijnCnEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().mijnCnEnabled
    }

    override fun getPapEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().papEnabled
    }
}
