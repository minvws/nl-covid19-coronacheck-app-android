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
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.eu.models.EuPublicKeysResult
import nl.rijksoverheid.ctr.appconfig.eu.usecases.EuPublicKeyUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.PersistEuPublicKeysUsecase
import nl.rijksoverheid.ctr.appconfig.eu.usecases.StoreFileResult
import nl.rijksoverheid.ctr.appconfig.usecases.LoadPublicKeysUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

abstract class VerifierConfigViewModel: ViewModel() {
    abstract fun refresh()
}

class VerifierConfigViewModelImpl(private val euPublicKeyUsecase: EuPublicKeyUsecase,
                                  private val persistEuPublicKeysUsecase: PersistEuPublicKeysUsecase,
                                  private val loadPublicKeysUseCase: LoadPublicKeysUseCase,
                                  private val cachedAppConfigUseCase: CachedAppConfigUseCase,
                                  private val mobileCoreWrapper: MobileCoreWrapper,
                                  private val cacheDirPath: String,
): VerifierConfigViewModel() {
    override fun refresh() {

        if (euPublicKeyUsecase.areConfigFilesPresent() && euPublicKeyUsecase.checkEuPublicKeysValidity()) {
            // If we have public keys stored, load them so they can be used by CTCL
            cachedAppConfigUseCase.getCachedPublicKeys()?.let {
                loadPublicKeysUseCase.load(it)
                mobileCoreWrapper.initializeVerifier(cacheDirPath)
            }
        } else {
            viewModelScope.launch {
                when (val publicKeysResult = euPublicKeyUsecase.retrieveEuPublicKeys()) {
                    is EuPublicKeysResult.Success -> {
                        val storePublicKeysResult = persistEuPublicKeysUsecase.persist("public_keys.json", publicKeysResult.publicKeys)
                        val storeConfigResult = persistEuPublicKeysUsecase.persist("config.json", publicKeysResult.config)

                        val storeSuccessful = storePublicKeysResult is StoreFileResult.Success && storeConfigResult is StoreFileResult.Success

                        if (storeSuccessful) {
                            mobileCoreWrapper.initializeVerifier(cacheDirPath)
                        } else {
                            //TODO error state
                        }
                    }
                    is EuPublicKeysResult.Error -> {
                        //TODO error state
                    }
                }
            }
        }
    }
}
