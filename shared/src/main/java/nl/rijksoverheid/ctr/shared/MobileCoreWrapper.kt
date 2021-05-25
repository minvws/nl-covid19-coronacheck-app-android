/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import java.lang.reflect.Type

interface MobileCoreWrapper {
    fun loadIssuerPks(bytes: ByteArray)
    fun createCredentials(body: ByteArray): String
    fun readCredential(credentials: ByteArray): ByteArray
    fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String
    fun discloseAllWithTimeQrEncoded(secretKey: ByteArray, credentials: ByteArray): String
    fun generateHolderSk(): String
    fun getDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential>
}

class MobileCoreWrapperImpl(private val moshi: Moshi) : MobileCoreWrapper {
    override fun loadIssuerPks(bytes: ByteArray) {
        Mobilecore.loadDomesticIssuerPks(bytes)
    }

    override fun createCredentials(body: ByteArray): String {
        return Mobilecore.createCredentials(
            body
        ).successString()
    }

    override fun readCredential(credentials: ByteArray): ByteArray {
        return Mobilecore.readDomesticCredential(credentials).verify()
    }

    override fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String {
        return Mobilecore.createCommitmentMessage(
            secretKey,
            nonce
        ).successString()
    }

    override fun discloseAllWithTimeQrEncoded(
        secretKey: ByteArray,
        credentials: ByteArray
    ): String {
        return Mobilecore.discloseAllWithTimeQrEncoded(
            secretKey,
            credentials
        ).successString()
    }

    override fun generateHolderSk(): String {
        return Mobilecore.generateHolderSk().successString()
    }

    override fun getDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential> {
        val createCredentialsResult =
            Mobilecore.createCredentials(createCredentials).successString()

        val type: Type = Types.newParameterizedType(
            List::class.java,
            DomesticCredential::class.java
        )

        val adapter: JsonAdapter<List<DomesticCredential>> = moshi.adapter(type)
        return adapter.fromJson(createCredentialsResult)
            ?: throw IllegalStateException("Could not create domestic credentials")
    }
}
