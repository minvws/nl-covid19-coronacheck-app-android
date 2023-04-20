package nl.rijksoverheid.ctr.holder.data_migration

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem

val transferOutOnboardingItems = listOf(
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

val transferInOnboardingItems = listOf(
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

class DataMigrationInstructionsFragment
