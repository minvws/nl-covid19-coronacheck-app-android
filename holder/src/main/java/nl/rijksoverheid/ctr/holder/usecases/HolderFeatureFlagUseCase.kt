/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.usecases

import java.time.Clock
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase

interface HolderFeatureFlagUseCase {
    fun getGgdEnabled(): Boolean
    fun getMijnCnEnabled(): Boolean
    fun getPapEnabled(): Boolean
    fun getAddEventsButtonEnabled(): Boolean
    fun getScanCertificateButtonEnabled(): Boolean
    fun getMigrateButtonEnabled(): Boolean
    fun isInArchiveMode(): Boolean
}

class HolderFeatureFlagUseCaseImpl(
    private val clock: Clock,
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

    override fun getAddEventsButtonEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().addEventsButtonEnabled ?: true
    }

    override fun getScanCertificateButtonEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().scanCertificateButtonEnabled ?: true
    }

    override fun getMigrateButtonEnabled(): Boolean {
        return cachedAppConfigUseCase.getCachedAppConfig().migrateButtonEnabled ?: true
    }

    override fun isInArchiveMode(): Boolean {
        val archiveOnlyDate = cachedAppConfigUseCase.getCachedAppConfig().archiveOnlyDate ?: return false
        val now = OffsetDateTime.now(clock)
        return now.isAfter(archiveOnlyDate)
    }
}
