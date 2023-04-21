/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDataMigrationTransferOptionsBinding
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

val transferOutOnboardingItems = arrayOf(
    OnboardingItem(
        imageResource = R.drawable.illustration_data_migration_instructions_step_1,
        titleResource = R.string.holder_startMigration_toOtherDevice_onboarding_step1_title,
        description = R.string.holder_startMigration_toOtherDevice_onboarding_step1_message,
        position = 1
    ),
    OnboardingItem(
        imageResource = R.drawable.illustration_data_migration_instructions_step_2,
        titleResource = R.string.holder_startMigration_toOtherDevice_onboarding_step2_title,
        description = R.string.holder_startMigration_toOtherDevice_onboarding_step2_message,
        position = 2
    )
)

val transferInOnboardingItems = arrayOf(
    OnboardingItem(
        imageResource = R.drawable.illustration_data_migration_instructions_step_1,
        titleResource = R.string.holder_startMigration_toThisDevice_onboarding_step1_title,
        description = R.string.holder_startMigration_toThisDevice_onboarding_step1_message,
        position = 1
    ),
    OnboardingItem(
        imageResource = R.drawable.illustration_data_migration_instructions_step_2,
        titleResource = R.string.holder_startMigration_toThisDevice_onboarding_step2_title,
        description = R.string.holder_startMigration_toThisDevice_onboarding_step2_message,
        position = 2
    )
)

class DataMigrationTransferOptionsFragment : Fragment(R.layout.fragment_data_migration_transfer_options) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDataMigrationTransferOptionsBinding.bind(view)

        binding.transferOutButton.transferOptionsButtonTitle.text = getString(R.string.holder_startMigration_option_toOtherDevice_title)
        binding.transferOutButton.transferOptionsButtonIcon.setImageResource(R.drawable.transfer_out)
        binding.transferInButton.transferOptionsButtonTitle.text = getString(R.string.holder_startMigration_option_toThisDevice_title)
        binding.transferInButton.transferOptionsButtonIcon.setImageResource(R.drawable.transfer_in)

        binding.transferOutButton.root.setOnClickListener {
            navigateSafety(
                DataMigrationTransferOptionsFragmentDirections.actionDataMigrationInstructions(
                    instructionItems = transferOutOnboardingItems,
                    destination = DataMigrationOnboardingItem.ShowQrCode
                )
            )
        }

        binding.transferInButton.root.setOnClickListener {
            navigateSafety(
                DataMigrationTransferOptionsFragmentDirections.actionDataMigrationInstructions(
                    instructionItems = transferInOnboardingItems,
                    destination = DataMigrationOnboardingItem.ScanQrCode
                )
            )
        }
    }
}
