/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.utils

import org.json.JSONObject

interface PaperProofUtil {
    fun getSignerCredential(
        qrContent: String,
        couplingCode: String? = null
    ): ByteArray
}

class PaperProofUtilImpl: PaperProofUtil {

    override fun getSignerCredential(
        qrCode: String,
        couplingCode: String?
    ): ByteArray = JSONObject(
        mutableMapOf("credential" to qrCode)
            .also { map ->
                couplingCode?.let { couplingCode ->
                    map["couplingCode"] = couplingCode
                }
            }
            .toMap())
        .toString()
        .toByteArray()
}