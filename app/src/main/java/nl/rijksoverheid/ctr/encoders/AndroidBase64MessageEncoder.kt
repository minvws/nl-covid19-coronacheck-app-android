package nl.rijksoverheid.ctr.encoders

import android.util.Base64
import com.goterl.lazycode.lazysodium.interfaces.MessageEncoder

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * LazySodium library uses [com.goterl.lazycode.lazysodium.utils.Base64MessageEncoder]
 * but this only works on sdk < 26 because of java.util.Base64. android.util.Base64 should be used.
 */
class AndroidBase64MessageEncoder : MessageEncoder {

    override fun encode(cipher: ByteArray?): String {
        return Base64.encodeToString(cipher, Base64.NO_WRAP)
    }

    override fun decode(cipherText: String?): ByteArray {
        return Base64.decode(cipherText, Base64.NO_WRAP)
    }
}
