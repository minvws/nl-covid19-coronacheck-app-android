/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.policy

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.usecases.VerifierFeatureFlagUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class NewPolicyRulesItemUseCaseImplTest {

    private val featureFlagUseCase: VerifierFeatureFlagUseCase = mockk()
    private val newPolicyRulesItemUseCase = NewPolicyRulesItemUseCaseImpl(featureFlagUseCase)

    @Test
    fun `when verification policy selection is enabled show multiple policy text`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns true

        with(newPolicyRulesItemUseCase.get()) {
            assertEquals(R.string.new_in_app_risksetting_title, title)
            assertEquals(R.string.new_in_app_risksetting_subtitle, description)
        }
    }

    @Test
    fun `when verification policy selection is disabled show single policy text`() {
        every { featureFlagUseCase.isVerificationPolicySelectionEnabled() } returns false

        with(newPolicyRulesItemUseCase.get()) {
            assertEquals(R.string.new_policy_1G_title, title)
            assertEquals(R.string.new_policy_1G_subtitle, description)
        }
    }
}
