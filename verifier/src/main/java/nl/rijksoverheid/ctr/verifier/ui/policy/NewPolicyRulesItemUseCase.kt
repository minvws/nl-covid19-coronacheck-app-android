/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.verifier.R

interface NewPolicyRulesItemUseCase {
        fun get(): NewPolicyItem
}

class NewPolicyRulesItemUseCaseImpl(
    private val featureFlagUseCase: FeatureFlagUseCase
) : NewPolicyRulesItemUseCase {

    override fun get(): NewPolicyItem {
        val title = if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
            R.string.new_in_app_risksetting_title
        } else {
            R.string.new_policy_1G_title
        }
        val description = if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
            R.string.new_in_app_risksetting_subtitle
        } else {
            R.string.new_policy_1G_subtitle
        }
        return NewPolicyItem(title, description)
    }
}
