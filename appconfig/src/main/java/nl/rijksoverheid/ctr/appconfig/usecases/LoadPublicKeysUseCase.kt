package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import okio.BufferedSource
import okio.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface LoadPublicKeysUseCase {
    fun load(publicKeysBufferedSource: BufferedSource)
}

class LoadPublicKeysUseCaseImpl(
    private val moshi: Moshi,
    private val mobileCoreWrapper: MobileCoreWrapper
) : LoadPublicKeysUseCase {

    override fun load(publicKeysBufferedSource: BufferedSource) {
        val publicKeys = moshi.adapter(PublicKeys::class.java).fromJson(publicKeysBufferedSource)
        val json = moshi.adapter(List::class.java).toJson(publicKeys!!.clKeys)
        mobileCoreWrapper.loadIssuerPks(json.toByteArray())
    }
}
