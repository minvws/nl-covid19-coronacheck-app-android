/*
 *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class LoginResult {
    data class Success(val jwt: String): LoginResult()
    data class Failed(val errorResult: ErrorResult): LoginResult()
    object Cancelled: LoginResult()
    object TokenUnavailable: LoginResult()
    object NoBrowserFound: LoginResult()
}