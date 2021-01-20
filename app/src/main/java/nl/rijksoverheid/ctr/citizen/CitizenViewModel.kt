package nl.rijksoverheid.ctr.citizen

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.data.factory.DependencyFactory
import nl.rijksoverheid.ctr.data.models.EventQR
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.data.models.User
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CitizenViewModel : ViewModel() {

    private val api = DependencyFactory().getTestApiClient()
    private val moshi = DependencyFactory().getMoshi()
    val userLiveData = MutableLiveData<Result<User>>()
    val qrCodeLiveData = MutableLiveData<Result<Bitmap>>()

    private fun getUserId(): String {
        val userResult = userLiveData.value
        if (userResult is Result.Success) {
            return userResult.data.id
        } else {
            throw IllegalStateException("User is not logged in")
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                userLiveData.postValue(Result.Success(User("ef9f409a-8613-4600-b135-8d2ac12559b3")))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun generateQrCode(eventQrJson: String) {
        viewModelScope.launch {
            try {
                val eventQR = moshi.adapter(EventQR::class.java).fromJson(eventQrJson)
                val testResults = api.getTestResults(getUserId())
                Timber.v("TEST RESULTS: " + testResults)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

}
