/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.verifier.ui.scanner.util

import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanqr.util.MenuUtilImpl
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuUtilImplTest: AutoCloseKoinTest() {

    @Test
    fun `When verification policy selection is enabled return correct menu items`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
        val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)
        val featureFlagUseCase = mockk<FeatureFlagUseCase>()

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager,
            featureFlagUseCase = featureFlagUseCase
        )

        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } answers { true }

        val menuSections = util.getMenuSections(
            context = ApplicationProvider.getApplicationContext()
        )

        Assert.assertEquals(1, menuSections.size)
        Assert.assertEquals(4, menuSections.first().menuItems.size)
    }

    @Test
    fun `When verification policy selection is disabled return correct menu items`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
        val appConfigPersistenceManager = mockk<AppConfigPersistenceManager>(relaxed = true)
        val featureFlagUseCase = mockk<FeatureFlagUseCase>()

        val util = MenuUtilImpl(
            cachedAppConfigUseCase = cachedAppConfigUseCase,
            appConfigPersistenceManager = appConfigPersistenceManager,
            featureFlagUseCase = featureFlagUseCase
        )

        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } answers { false }

        val menuSections = util.getMenuSections(
            context = ApplicationProvider.getApplicationContext()
        )

        Assert.assertEquals(1, menuSections.size)
        Assert.assertEquals(3, menuSections.first().menuItems.size)
    }
}