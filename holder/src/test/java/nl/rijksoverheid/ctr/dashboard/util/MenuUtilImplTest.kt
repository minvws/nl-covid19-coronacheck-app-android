/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.util

import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.dashboard.util.MenuUtilImpl
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `When visitor pass enabled return correct menu sections`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
        val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>(relaxed = true)

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager,
            featureFlagUseCase = featureFlagUseCase
        )

        every { featureFlagUseCase.getVisitorPassEnabled() } returns true

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
        val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>(relaxed = true)

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager,
            featureFlagUseCase = featureFlagUseCase
        )

        every { featureFlagUseCase.getVisitorPassEnabled() } returns false

        val menuSections = util.getMenuSections(
            context = ApplicationProvider.getApplicationContext()
        )

        assertEquals(2, menuSections[0].menuItems.size)
        assertEquals(2, menuSections[1].menuItems.size)
    }
}
