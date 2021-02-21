/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.api.json

import android.util.Base64
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class Base64JsonAdapter {
    @FromJson
    fun fromBase64(value: String): ByteArray = Base64.decode(value, Base64.DEFAULT)

    @ToJson
    fun toBase64(value: ByteArray) = Base64.encodeToString(value, Base64.NO_WRAP)
}
