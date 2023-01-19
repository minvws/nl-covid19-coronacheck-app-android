package nl.rijksoverheid.ctr.design.menu.about

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
data class HelpdeskData(
    val contactTitle: String,
    val contactMessage: String,
    val supportTitle: String,
    val supportMessage: String,
    val appVersionTitle: String,
    val appVersion: String,
    val configurationTitle: String,
    val configuration: String
) : Parcelable
