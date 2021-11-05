package nl.rijksoverheid.ctr.appconfig.models

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class ExternalReturnAppData(
    val appName: String,
    val intent: Intent
) : Parcelable