/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject

interface GetDccFromEuropeanCredentialUseCase {
    fun get(europeanCredential: ByteArray): JSONObject
}

class GetDccFromEuropeanCredentialUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper
) : GetDccFromEuropeanCredentialUseCase {

    override fun get(europeanCredential: ByteArray): JSONObject {
        val credentials = mobileCoreWrapper.readEuropeanCredential(europeanCredential)
        return requireNotNull(credentials.optJSONObject("dcc"))
    }
}
