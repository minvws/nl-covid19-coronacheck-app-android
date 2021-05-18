package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

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
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DigiDViewModel(private val authenticationRepository: AuthenticationRepository) : ViewModel() {

    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val accessTokenLiveData = MutableLiveData<Event<String>>()

    fun login(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        authService: AuthorizationService
    ) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            authenticationRepository.authResponse(activityResultLauncher, authService)
            loading.value = Event(false)
        }
    }

    fun handleActivityResult(activityResult: ActivityResult, authService: AuthorizationService) {
        viewModelScope.launch {
            val intent = activityResult.data
            if (intent != null) {
                val authResponse = AuthorizationResponse.fromIntent(intent)
                val authError = AuthorizationException.fromIntent(intent)
                when {
                    authError != null -> {
                    }
                    authResponse != null -> {
                        val accessToken =
                            authenticationRepository.accessToken(authService, authResponse)
                        accessTokenLiveData.postValue(Event(accessToken))
                    }
                    else -> {

                    }
                }
            } else {

            }
        }
    }
}
