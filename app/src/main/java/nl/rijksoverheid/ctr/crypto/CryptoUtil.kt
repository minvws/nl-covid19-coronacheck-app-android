package nl.rijksoverheid.ctr.crypto

import org.libsodium.jni.NaCl
import org.libsodium.jni.Sodium
import java.nio.charset.Charset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CryptoUtil {

    private val charset: Charset = Charsets.UTF_8

    init {
        NaCl.sodium()
    }

    fun signVerifyDetached(
        signatureBytes: ByteArray,
        message: String,
        publicKeyBytes: ByteArray
    ): Boolean {
        val messageBytes = bytes(message)
        return Sodium.crypto_sign_verify_detached(
            signatureBytes,
            messageBytes,
            messageBytes.size,
            publicKeyBytes
        ) == 0
    }

    private fun bytes(s: String): ByteArray {
        return s.toByteArray(charset)
    }
}
