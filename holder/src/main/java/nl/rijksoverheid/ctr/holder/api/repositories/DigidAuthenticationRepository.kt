package nl.rijksoverheid.ctr.holder.api.repositories

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.*
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DigidAuthenticationRepository: AuthenticationRepository {

    override suspend fun authResponse(
        loginType: LoginType,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    ) {
        val authServiceConfiguration = authorizationServiceConfiguration(loginType)
        val authRequest = authRequest(serviceConfiguration = authServiceConfiguration)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        activityResultLauncher.launch(authIntent)
    }

    private suspend fun authorizationServiceConfiguration(loginType: LoginType): AuthorizationServiceConfiguration {
        val url = when (loginType) {
            is LoginType.Max -> BuildConfig.DIGI_D_BASE_URL
            is LoginType.Pap -> BuildConfig.GGD_BASE_URL
        }
        return suspendCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(url)) { serviceConfiguration, error ->
                when {
                    error != null -> continuation.resumeWithException(error)
                    serviceConfiguration != null -> continuation.resume(serviceConfiguration)
                    else -> throw Exception("Could not get service configuration")
                }
            }
        }
    }

    private fun authRequest(serviceConfiguration: AuthorizationServiceConfiguration): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfiguration,
            BuildConfig.DIGI_D_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(BuildConfig.DIGI_D_REDIRECT_URI)
        ).setScope("openid email profile").build()
    }

    override suspend fun jwt(
        loginType: LoginType,
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): String {
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, error ->
                val jwt = when (loginType) {
                    is LoginType.Max -> resp?.idToken
                    is LoginType.Pap -> resp?.accessToken
                }
                when {
                    jwt != null -> continuation.resume(jwt)
                    error != null -> continuation.resumeWithException(error)
                    else -> continuation.resumeWithException(Exception("Could not get jwt"))
                }
            }
        }
    }
}
