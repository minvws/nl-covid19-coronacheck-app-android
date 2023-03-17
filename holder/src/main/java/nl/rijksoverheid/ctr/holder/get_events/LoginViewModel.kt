/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.models.HolderStep.DigidNetworkRequest
import nl.rijksoverheid.ctr.shared.exceptions.OpenIdAuthorizationException
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.models.OpenIdErrorResult.Error
import nl.rijksoverheid.ctr.shared.models.OpenIdErrorResult.ServerBusy
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.rdo.modules.openidconnect.OpenIDConnectRepository

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class LoginViewModel(
    private val digidAuthenticationRepository: OpenIDConnectRepository,
    private val androidUtil: AndroidUtil
) : ViewModel() {

    private companion object {
        const val USER_CANCELLED_FLOW_CODE = 1
        const val NETWORK_ERROR = 3
        const val LOGIN_REQUIRED_ERROR = "login_required"
        const val SAML_AUTHN_FAILED_ERROR = "saml_authn_failed"
        const val CANCELLED = "cancelled"
    }

    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val loginResultLiveData = MutableLiveData<Event<LoginResult>>()

    fun login(
        loginType: LoginType,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    ) {
        (loading as MutableLiveData).value = Event(true)
        val issuerUrl = when (loginType) {
            LoginType.Max -> BuildConfig.MAX_BASE_URL
            LoginType.Pap -> BuildConfig.PAP_BASE_URL
        }
        viewModelScope.launch {
            try {
                digidAuthenticationRepository.requestAuthorization(issuerUrl, activityResultLauncher, authService)
            } catch (e: Exception) {
                postExceptionResult(e)
                loading.value = Event(false)
            }
        }
    }

    fun handleActivityResult(loginType: LoginType, activityResult: ActivityResult, authService: AuthorizationService) {
        viewModelScope.launch {
            val intent = activityResult.data
            if (intent != null) {
                val authResponse = AuthorizationResponse.fromIntent(intent)
                val authError = AuthorizationException.fromIntent(intent)
                when {
                    authError != null -> postAuthErrorResult(authError)
                    authResponse != null -> postAuthResponseResult(loginType, authService, authResponse)
                    else -> postAuthNullResult()
                }
            } else {
                loginResultLiveData.postValue(Event(LoginResult.Cancelled))
            }
        }
    }

    private fun postAuthErrorResult(authError: AuthorizationException) {
        val digidResult = when {
            isUserCancelled(authError) -> LoginResult.Cancelled
            isNetworkError(authError) -> getNetworkErrorResult(authError)
            isServerBusy(authError) -> getServerBusyResult(authError)
            else -> LoginResult.Failed(Error(DigidNetworkRequest, mapToOpenIdException(authError)))
        }
        loginResultLiveData.postValue(Event(digidResult))
    }

    private fun mapToOpenIdException(authError: AuthorizationException) =
        OpenIdAuthorizationException(type = authError.type, code = authError.code)

    private fun isNetworkError(authError: AuthorizationException): Boolean {
        return authError.type == AuthorizationException.TYPE_GENERAL_ERROR && authError.code == NETWORK_ERROR
    }

    private fun getNetworkErrorResult(authError: AuthorizationException): LoginResult {
        return if (!androidUtil.isNetworkAvailable()) {
            LoginResult.Failed(NetworkRequestResult.Failed.ClientNetworkError(DigidNetworkRequest))
        } else {
            LoginResult.Failed(
                NetworkRequestResult.Failed.ServerNetworkError(
                    DigidNetworkRequest,
                    mapToOpenIdException(authError)
                )
            )
        }
    }

    private fun isServerBusy(authError: AuthorizationException) =
        authError.error == LOGIN_REQUIRED_ERROR

    private fun getServerBusyResult(authError: AuthorizationException) =
        LoginResult.Failed(
            ServerBusy(DigidNetworkRequest, mapToOpenIdException(authError))
        )

    private fun isUserCancelled(authError: AuthorizationException) =
        (authError.type == AuthorizationException.TYPE_GENERAL_ERROR && authError.code == USER_CANCELLED_FLOW_CODE) ||
                authError.error == SAML_AUTHN_FAILED_ERROR ||
                authError.error == CANCELLED

    private suspend fun postAuthResponseResult(
        loginType: LoginType,
        authService: AuthorizationService,
        authResponse: AuthorizationResponse
    ) {
        try {
            val tokenResponse =
                digidAuthenticationRepository.tokenResponse(authService, authResponse)
            val jwt = when (loginType) {
                LoginType.Max -> tokenResponse.idToken!!
                LoginType.Pap -> tokenResponse.accessToken!!
            }
            loginResultLiveData.postValue(Event(LoginResult.Success(jwt)))
        } catch (e: Exception) {
            postExceptionResult(e)
        }
    }

    private fun postExceptionResult(e: Exception) {
        if (e is AuthorizationException) {
            postAuthErrorResult(e)
        } else {
            // App Auth will launch a browser intent to log in with DigiD.
            // When it throws an ActivityNotFoundException it means there is no browser app to handle the intent.
            val result = if (e is ActivityNotFoundException) {
                LoginResult.NoBrowserFound
            } else {
                LoginResult.Failed(Error(DigidNetworkRequest, e))
            }
            loginResultLiveData.postValue(Event(result))
        }
    }

    private fun postAuthNullResult() {
        loginResultLiveData.postValue(
            Event(LoginResult.Failed(Error(DigidNetworkRequest, NullPointerException())))
        )
    }
}
