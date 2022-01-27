/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.MenuUtilImpl
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuUtilImplTest {

    @Test
    fun `When visitor pass enabled return correct menu sections`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
        val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager
        )

        every { cachedAppConfigUseCase.getCachedAppConfig() } answers { HolderConfig.default(visitorPassEnabled = true) }

        val menuSections = util.getMenuSections(
            context = ApplicationProvider.getApplicationContext()
        )

        assertEquals(1, menuSections[0].menuItems.size)
        assertEquals(2, menuSections[1].menuItems.size)
        assertEquals(2, menuSections[2].menuItems.size)
    }

    @Test
    fun `When visitor pass disabled return correct menu sections`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
        val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager
        )

        every { cachedAppConfigUseCase.getCachedAppConfig() } answers { HolderConfig.default(visitorPassEnabled = false) }

        val menuSections = util.getMenuSections(
            context = ApplicationProvider.getApplicationContext()
        )

        assertEquals(2, menuSections[0].menuItems.size)
        assertEquals(2, menuSections[1].menuItems.size)
    }
}