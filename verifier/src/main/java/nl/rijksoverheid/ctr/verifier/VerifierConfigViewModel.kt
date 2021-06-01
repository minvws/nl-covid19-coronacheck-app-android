/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.eu.models.EuPublicKeysResult
import nl.rijksoverheid.ctr.appconfig.eu.usecases.EuPublicKeyUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.PersistEuPublicKeysUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.StoreFileResult
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

abstract class VerifierConfigViewModel: ViewModel() {
    abstract fun refresh()
}

class VerifierConfigViewModelImpl(private val euPublicKeyUsecase: EuPublicKeyUsecase,
                                  private val persistEuPublicKeysUsecase: PersistEuPublicKeysUsecase,
                                  private val mobileCoreWrapper: MobileCoreWrapper,
                                  private val cacheDirPath: String,
): VerifierConfigViewModel() {
    override fun refresh() {
        viewModelScope.launch {
            val publicKeysResult = euPublicKeyUsecase.retrieveEuPublicKeys()

            when (publicKeysResult) {
                is EuPublicKeysResult.Success -> {
                    val storePublicKeysResult = persistEuPublicKeysUsecase.persist("public_keys.json", publicKeysResult.publicKeys)
                    val storeConfigResult = persistEuPublicKeysUsecase.persist("config.json", publicKeysResult.config)

                    val storeSuccessful = storePublicKeysResult is StoreFileResult.Success && storeConfigResult is StoreFileResult.Success

                    if (storeSuccessful) {
                        mobileCoreWrapper.initializeVerifier(cacheDirPath)
                    } else {
                        println("GIO Errror")
                    }
                }
                is EuPublicKeysResult.Error -> {

                }
            }
        }

    }
}

