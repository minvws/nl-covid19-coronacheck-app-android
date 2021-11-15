package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.*
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.mijncn.MijnCNTokenResponse
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.json.JSONObject
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
class MijnCNAuthenticationRepository(
    private val testProviderApiClient: TestProviderApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory
) {

    suspend fun authResponse(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    ) {
        val authServiceConfiguration = authorizationServiceConfiguration()
        val authRequest = authRequest(serviceConfiguration = authServiceConfiguration)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        activityResultLauncher.launch(authIntent)
    }

    private suspend fun authorizationServiceConfiguration(): AuthorizationServiceConfiguration {
        return suspendCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(BuildConfig.MIJNCN_BASEURL)) { serviceConfiguration, error ->
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
            Uri.parse(BuildConfig.MIJNCN_REDIRECT_URI)
        ).setScope("openid email profile").build()
    }

    suspend fun jwt(
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): String {
        val request = authResponse.createTokenExchangeRequest()
        val res = retrieveAccessToken(request)
        return suspendCoroutine { continuation ->
            when (res) {
                is NetworkRequestResult.Success -> continuation.resume(res.response.id_token)
                is NetworkRequestResult.Failed -> continuation.resumeWithException(Exception("We failed"))
            }

//            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, error ->
//                val jwt = resp?.idToken
//                when {
//                    jwt != null -> continuation.resume(jwt)
//                    error != null -> continuation.resumeWithException(error)
//                    else -> continuation.resumeWithException(Exception("Could not get jwt"))
//                }
//            }
        }
    }

    suspend fun retrieveAccessToken(tokenRequest: TokenRequest): NetworkRequestResult<MijnCNTokenResponse> {
        val result =
            networkRequestResultFactory.createResult(HolderStep.AccessTokensNetworkRequest) {
                testProviderApiClient.getAccessToken(
                    url = tokenRequest.configuration.tokenEndpoint.toString(),
                    code = tokenRequest.authorizationCode ?: "",
                    grantType = tokenRequest.grantType,
                    redirectUri = BuildConfig.MIJNCN_REDIRECT_URI,
                    codeVerifier = tokenRequest.codeVerifier ?: "",
                    clientId = tokenRequest.clientId
                )
            }
        return result
    }

}
