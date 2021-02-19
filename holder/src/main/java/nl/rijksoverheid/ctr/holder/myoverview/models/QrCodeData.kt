package nl.rijksoverheid.ctr.holder.myoverview.models

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.holder.models.LocalTestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class QrCodeData(
    val localTestResult: LocalTestResult,
    val qrCode: Bitmap
)
