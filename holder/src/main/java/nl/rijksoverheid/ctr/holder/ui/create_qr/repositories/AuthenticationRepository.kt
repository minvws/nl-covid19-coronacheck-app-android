/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

interface AuthenticationRepository {

    suspend fun authResponse(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    )

    suspend fun jwt(
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): String
}