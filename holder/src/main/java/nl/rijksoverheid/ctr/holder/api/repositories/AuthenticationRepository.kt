/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.api.repositories

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType

interface AuthenticationRepository {

    suspend fun authResponse(
        loginType: LoginType,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    )

    suspend fun jwt(
        loginType: LoginType,
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): String
}
