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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagUseCaseImplTest {

    @Test
    fun `isVerificationPolicySelectionEnabled returns false if verificationPoliciesEnabled is empty`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } answers { emptyList() }

        val usecase = FeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicySelectionEnabled())
    }

    @Test
    fun `isVerificationPolicySelectionEnabled returns false if verificationPoliciesEnabled has one element`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } answers { listOf("3G") }

        val usecase = FeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicySelectionEnabled())
    }

    @Test
    fun `isVerificationPolicySelectionEnabled returns true if verificationPoliciesEnabled has two elements`() {
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPoliciesEnabled } answers { listOf("1G", "3G") }

        val usecase = FeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertTrue(usecase.isVerificationPolicySelectionEnabled())
    }
}