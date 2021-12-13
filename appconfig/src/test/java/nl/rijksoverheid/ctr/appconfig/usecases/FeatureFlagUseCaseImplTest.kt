/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */


package nl.rijksoverheid.ctr.appconfig.usecases

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagUseCaseImplTest {

    @Test
    fun `isVerificationPolicyEnabled returns false if enableVerificationPolicyVersion is lower than app version`() {
        val buildConfigUseCase = mockk<BuildConfigUseCase>()
        every { buildConfigUseCase.getVersionCode() } answers { 10 }
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion } answers { 11 }

        val usecase = FeatureFlagUseCaseImpl(
            buildConfigUseCase,
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicyEnabled())
    }

    @Test
    fun `isVerificationPolicyEnabled returns true if enableVerificationPolicyVersion is equal to app version`() {
        val buildConfigUseCase = mockk<BuildConfigUseCase>()
        every { buildConfigUseCase.getVersionCode() } answers { 10 }
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion } answers { 10 }

        val usecase = FeatureFlagUseCaseImpl(
            buildConfigUseCase,
            cachedAppConfigUseCase
        )

        assertTrue(usecase.isVerificationPolicyEnabled())
    }

    @Test
    fun `isVerificationPolicyEnabled returns true if enableVerificationPolicyVersion is higher than app version`() {
        val buildConfigUseCase = mockk<BuildConfigUseCase>()
        every { buildConfigUseCase.getVersionCode() } answers { 10 }
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion } answers { 9 }

        val usecase = FeatureFlagUseCaseImpl(
            buildConfigUseCase,
            cachedAppConfigUseCase
        )

        assertTrue(usecase.isVerificationPolicyEnabled())
    }
}