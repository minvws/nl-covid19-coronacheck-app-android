package nl.rijksoverheid.ctr.holder.repositories

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.*
import nl.rijksoverheid.ctr.BuildConfig
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
class AuthenticationRepository {

    suspend fun login(activity: AppCompatActivity): String {
        val authService = AuthorizationService(activity)
        val authServiceConfiguration = authorizationServiceConfiguration()
        val authRequest = authRequest(serviceConfiguration = authServiceConfiguration)
        val authResponse = authResponse(
            activity = activity,
            authService = authService,
            authRequest = authRequest
        )
        return accessToken(
            authService = authService,
            authResponse = authResponse
        )
    }

    private suspend fun authorizationServiceConfiguration(): AuthorizationServiceConfiguration {
        return suspendCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(BuildConfig.DIGI_D_BASE_URL)) { serviceConfiguration, error ->
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
            Uri.parse("coronatester-app-dev://auth/login")
        ).build()
    }

    private suspend fun authResponse(
        activity: AppCompatActivity,
        authService: AuthorizationService,
        authRequest: AuthorizationRequest
    ): AuthorizationResponse {
        return suspendCoroutine { continuation ->
            val startForResult =
                activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val intent = it.data
                    if (intent != null) {
                        val authResponse = AuthorizationResponse.fromIntent(intent)
                        val authError = AuthorizationException.fromIntent(intent)
                        when {
                            authError != null -> continuation.resumeWithException(authError)
                            authResponse != null -> continuation.resume(authResponse)
                            else -> continuation.resumeWithException(Exception("Could not get AuthorizationResponse"))
                        }
                    } else {
                        continuation.resumeWithException(Exception("Could not get AuthorizationResponse"))
                    }
                }

            val authIntent = authService.getAuthorizationRequestIntent(authRequest)
            startForResult.launch(authIntent)
        }
    }

    private suspend fun accessToken(
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): String {
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, error ->
                val accessToken = resp?.accessToken
                when {
                    accessToken != null -> continuation.resume(accessToken)
                    error != null -> continuation.resumeWithException(error)
                    else -> continuation.resumeWithException(Exception("Could not get AccessToken"))
                }
            }
        }
    }
}
