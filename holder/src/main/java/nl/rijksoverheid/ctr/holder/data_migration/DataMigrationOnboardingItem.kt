/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem

sealed class DataMigrationOnboardingItem(
    @StringRes val title: Int,
    @StringRes val descriptionResource: Int,
    @IdRes val navigationActionId: Int
) : OnboardingItem(
    titleResource = title,
    description = descriptionResource
) {
    @Parcelize
    object ShowQrCode : DataMigrationOnboardingItem(
        title = R.string.holder_startMigration_toOtherDevice_onboarding_step3_title,
        descriptionResource = R.string.holder_startMigration_toOtherDevice_onboarding_step3_title,
        navigationActionId = R.id.action_my_overview
    ), Parcelable

    @Parcelize
    object ScanQrCode : DataMigrationOnboardingItem(
        title = R.string.holder_startMigration_toThisDevice_onboarding_step3_title,
        descriptionResource = R.string.holder_startMigration_toThisDevice_onboarding_step3_title,
        navigationActionId = R.id.action_data_migration_scan_qr
    ), Parcelable
}
