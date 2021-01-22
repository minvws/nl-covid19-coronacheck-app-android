package nl.rijksoverheid.ctr.factories

import android.util.Base64
import com.goterl.lazycode.lazysodium.utils.Key

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
object KeyFactory {

    /**
     * Same as [Key.fromBase64String] but that class uses java.util.Base64 which is only supported on
     * sdk < 26. Base64 from android.util should be used.
     */
    fun createKeyFromBase64String(string: String): Key {
        return Key.fromBytes(Base64.decode(string, Base64.NO_WRAP))
    }
}
