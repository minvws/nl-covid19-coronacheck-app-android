package nl.rijksoverheid.ctr.shared.usecases

import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.models.Issuers
import nl.rijksoverheid.ctr.shared.models.JSON

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SignatureValidUseCase(
    private val cryptoUtil: CryptoUtil,
    private val moshi: Moshi,
) {

    fun checkValid(
        issuers: List<Issuers.Issuer>,
        signature: String,
        data: JSON
    ) {
        issuers.firstOrNull { issuer ->
            cryptoUtil.signVerifyDetached(
                Base64.decode(signature, Base64.NO_WRAP),
                data.toJson(moshi),
                Base64.decode(issuer.publicKey, Base64.NO_WRAP)
            )
        } ?: throw Exception("Signature not valid")
    }
}
