package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.shared.ClmobileWrapper

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

class LoadPublicKeysUseCaseImpl(private val moshi: Moshi, private val clmobileWrapper: ClmobileWrapper) : LoadPublicKeysUseCase {

    override fun load(publicKeys: PublicKeys) {
        val json = moshi.adapter(List::class.java).toJson(publicKeys.clKeys)
        clmobileWrapper.loadIssuerPks(json.toByteArray())
    }
}
