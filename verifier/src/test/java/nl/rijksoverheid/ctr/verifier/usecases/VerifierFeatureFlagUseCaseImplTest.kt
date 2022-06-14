/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */


package nl.rijksoverheid.ctr.verifier.usecases

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifierFeatureFlagUseCaseImplTest {

    @Test
    fun `isVerificationPolicySelectionEnabled returns false if verificationPoliciesEnabled is empty`() {
        val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPolicies } answers { emptyList() }

        val usecase = VerifierFeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicySelectionEnabled())
    }

    @Test
    fun `isVerificationPolicySelectionEnabled returns false if verificationPoliciesEnabled has one element`() {
        val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPolicies } answers { listOf("3G") }

        val usecase = VerifierFeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicySelectionEnabled())
    }

    @Test
    fun `isVerificationPolicySelectionEnabled returns true if verificationPoliciesEnabled has two elements`() {
        val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPolicies } answers { listOf("1G", "3G") }

        val usecase = VerifierFeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertTrue(usecase.isVerificationPolicySelectionEnabled())
    }

    @Test
    fun `isVerificationPolicySelectionEnabled returns false if verificationPoliciesEnabled has unsupported elements`() {
        val cachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        every { cachedAppConfigUseCase.getCachedAppConfig().verificationPolicies } answers { listOf("2G", "3G") }

        val usecase = VerifierFeatureFlagUseCaseImpl(
            cachedAppConfigUseCase
        )

        assertFalse(usecase.isVerificationPolicySelectionEnabled())
    }
}