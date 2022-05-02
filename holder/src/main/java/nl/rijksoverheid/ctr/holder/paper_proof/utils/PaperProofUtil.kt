/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.utils

import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetDccFromEuropeanCredentialUseCase
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import org.json.JSONObject

interface PaperProofUtil {
    fun getEventGroupJsonData(
        qrContent: String,
        couplingCode: String? = null
    ): ByteArray

    fun getIssuer(
        europeanCredential: ByteArray
    ): String
}

class PaperProofUtilImpl(
    private val getDccFromEuropeanCredentialUseCase: GetDccFromEuropeanCredentialUseCase
): PaperProofUtil {

    override fun getEventGroupJsonData(
        qrContent: String,
        couplingCode: String?
    ): ByteArray = JSONObject(
        mutableMapOf("credential" to qrContent)
            .also { map ->
                couplingCode?.let { couplingCode ->
                    map["couplingCode"] = couplingCode
                }
            }
            .toMap())
        .toString()
        .toByteArray()

    override fun getIssuer(europeanCredential: ByteArray): String {
        val dcc = getDccFromEuropeanCredentialUseCase.get(europeanCredential)
        return dcc.optJSONArray("v")?.optJSONObject(0)?.getStringOrNull("is") ?: ""
    }
}