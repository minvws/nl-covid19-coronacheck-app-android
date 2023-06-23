/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AppStatus : Parcelable {

    @Parcelize
    object UpdateRequired : AppStatus(), Parcelable

    @Parcelize
    object UpdateRecommended : AppStatus(), Parcelable

    @Parcelize
    object Deactivated : AppStatus(), Parcelable

    @Parcelize
    object Archived : AppStatus(), Parcelable

    @Parcelize
    object Error : AppStatus(), Parcelable

    @Parcelize
    data class LaunchError(val errorMessage: String) : AppStatus(), Parcelable

    @Parcelize
    object NoActionRequired : AppStatus(), Parcelable

    @Parcelize
    data class NewFeatures(val appUpdateData: AppUpdateData) : AppStatus(), Parcelable

    @Parcelize
    data class ConsentNeeded(val appUpdateData: AppUpdateData) : AppStatus(), Parcelable
}
