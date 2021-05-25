/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared

import clmobile.Clmobile
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.ext.verify

interface ClmobileWrapper {
    fun loadIssuerPks(bytes: ByteArray)
    fun createCredential(secretKey: ByteArray, body: ByteArray): String
    fun readCredential(credentials: ByteArray): ByteArray
    fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String
    fun discloseAllWithTimeQrEncoded(secretKey: ByteArray, credentials: ByteArray): String
    fun generateHolderSk(): String
}

class ClmobileWrapperImpl : ClmobileWrapper {
    override fun loadIssuerPks(bytes: ByteArray) {
        Clmobile.loadIssuerPks(bytes)
    }

    override fun createCredential(secretKey: ByteArray, body: ByteArray): String {
        return Clmobile.createCredential(
            secretKey,
            body
        ).successString()
    }

    override fun readCredential(credentials: ByteArray): ByteArray {
        return Clmobile.readCredential(credentials).verify()
    }

    override fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String {
        return Clmobile.createCommitmentMessage(
            secretKey,
            nonce
        ).successString()
    }

    override fun discloseAllWithTimeQrEncoded(
        secretKey: ByteArray,
        credentials: ByteArray
    ): String {
        return Clmobile.discloseAllWithTimeQrEncoded(
            secretKey,
            credentials
        ).successString()
    }

    override fun generateHolderSk(): String {
        return Clmobile.generateHolderSk().successString()
    }
}