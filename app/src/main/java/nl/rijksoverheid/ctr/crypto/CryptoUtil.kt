package nl.rijksoverheid.ctr.crypto

import android.util.Base64
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

    fun generatePublicAndPrivateKey(): Pair<ByteArray, ByteArray>? {
        val publicKey = randomBytesBuf(32)
        val privateKey = randomBytesBuf(32)

        return if (Sodium.crypto_box_keypair(publicKey, privateKey) == 0) {
            Pair(publicKey, privateKey)
        } else {
            null
        }
    }

    fun generateNonce(): ByteArray {
        return randomBytesBuf(24)
    }

    fun boxEasy(
        message: String,
        nonceBytes: ByteArray,
        publicKeyBytes: ByteArray,
        privateKeyBytes: ByteArray
    ): String? {
        val messageBytes = bytes(message)
        val cipherBytes = ByteArray(16 + messageBytes.size)

        return if (Sodium.crypto_box_easy(
                cipherBytes,
                messageBytes,
                messageBytes.size,
                nonceBytes,
                publicKeyBytes,
                privateKeyBytes
            ) == 0
        ) {
            encode(cipherBytes)
        } else {
            null
        }
    }

    fun boxOpenEasy(
        cipher: String,
        nonce: String,
        publicKey: String,
        privateKey: String
    ): String? {
        val cipherBytes = decode(cipher)
        val nonceBytes = decode(nonce)
        val publicKeyBytes = decode(publicKey)
        val privateKeyBytes = decode(privateKey)
        val messageBytes = ByteArray(cipherBytes.size - 16)

        return if (Sodium.crypto_box_open_easy(
                messageBytes,
                cipherBytes,
                cipherBytes.size,
                nonceBytes,
                publicKeyBytes,
                privateKeyBytes
            ) == 0
        ) {
            String(messageBytes)
        } else {
            null
        }
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

    private fun randomBytesBuf(size: Int): ByteArray {
        val bs = ByteArray(size)
        Sodium.randombytes_buf(bs, size)
        return bs
    }

    private fun bytes(s: String): ByteArray {
        return s.toByteArray(charset)
    }

    private fun encode(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun decode(s: String): ByteArray {
        return Base64.decode(s, Base64.NO_WRAP)
    }
}
