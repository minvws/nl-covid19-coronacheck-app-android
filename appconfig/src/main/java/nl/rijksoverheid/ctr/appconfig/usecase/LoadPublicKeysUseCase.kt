package nl.rijksoverheid.ctr.appconfig

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface LoadPublicKeysUseCase {
    fun load(publicKeys: PublicKeys)
}

class LoadPublicKeysUseCaseImpl(private val moshi: Moshi) : LoadPublicKeysUseCase {

    override fun load(publicKeys: PublicKeys) {
        val json = moshi.adapter(List::class.java).toJson(publicKeys.clKeys)
        Clmobile.loadIssuerPks(json.toByteArray())
    }
}
