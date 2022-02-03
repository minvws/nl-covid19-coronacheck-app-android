package nl.rijksoverheid.ctr.verifier.ui.instructions

import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun instructionsExplanationData(selectionState: VerificationPolicySelectionState): IntroductionData =
    IntroductionData(
        onboardingItems = listOf(
            OnboardingItem(
                animationResource = R.raw.scaninstructions_1,
                titleResource = R.string.scan_instructions_1_title,
                description = R.string.scan_instructions_1_description,
                position = 1
            ),
            OnboardingItem(
                animationResource = R.raw.scaninstructions_2,
                titleResource = R.string.scan_instructions_2_title,
                description = R.string.scan_instructions_2_description,
                position = 2
            ),
            OnboardingItem(
                animationResource = R.raw.scaninstructions_3,
                titleResource = R.string.scan_instructions_3_title,
                description = R.string.scan_instructions_3_description,
                position = 3
            ),
            OnboardingItem(
                animationResource = R.raw.scaninstructions_4,
                titleResource = when (selectionState) {
                    is VerificationPolicySelectionState.Selection,
                    is VerificationPolicySelectionState.Policy1G -> R.string.scan_instructions_4_title_1G
                    is VerificationPolicySelectionState.Policy3G -> R.string.scan_instructions_4_title
                },
                description = when (selectionState) {
                    is VerificationPolicySelectionState.Selection,
                    is VerificationPolicySelectionState.Policy1G -> R.string.scan_instructions_4_description_1G
                    is VerificationPolicySelectionState.Policy3G -> R.string.scan_instructions_4_description
                }
            ),
            OnboardingItem(
                animationResource = R.raw.scaninstructions_5,
                titleResource = R.string.scan_instructions_5_title,
                description = R.string.scan_instructions_5_description,
                position = 5
            ),
        )
    )