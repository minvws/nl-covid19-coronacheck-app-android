package nl.rijksoverheid.ctr.shared

import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
object QrCodeConstants {
    val VALID_FOR_SECONDS = TimeUnit.MINUTES.toSeconds(3)
}
