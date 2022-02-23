/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.instructions

import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState
import org.junit.Assert.assertEquals
import org.junit.Test

class InstructionsExplanationDataTest {

    @Test
    fun `on fourth scan instruction with selection state show 1G texts`() {
        val onboardingItem =
            instructionsExplanationData(
                VerificationPolicySelectionState.Selection.None
            ).find { it.animationResource == R.raw.scaninstructions_4 }

        assertEquals(R.string.scan_instructions_4_title_1G, onboardingItem!!.titleResource)
        assertEquals(R.string.scan_instructions_4_description_1G, onboardingItem.description)
    }

    @Test
    fun `on fourth scan instruction with 1G state show 1G texts`() {
        val onboardingItem =
            instructionsExplanationData(
                VerificationPolicySelectionState.Policy1G
            ).find { it.animationResource == R.raw.scaninstructions_4 }

        assertEquals(R.string.scan_instructions_4_title_1G, onboardingItem!!.titleResource)
        assertEquals(R.string.scan_instructions_4_description_1G, onboardingItem.description)
    }

    @Test
    fun `on fourth scan instruction with 3G state show 3G texts`() {
        val onboardingItem =
            instructionsExplanationData(
                VerificationPolicySelectionState.Policy3G
            ).find { it.animationResource == R.raw.scaninstructions_4 }

        assertEquals(R.string.scan_instructions_4_title, onboardingItem!!.titleResource)
        assertEquals(R.string.scan_instructions_4_description, onboardingItem.description)
    }
}