package nl.rijksoverheid.ctr.shared.ext

import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun mobilecore.Result.successString(): String {
    if (this.error.isNotEmpty()) {
        throw Exception(this.error)
    } else {
        return String(this.value)
    }
}

fun mobilecore.Result.successJsonObject(): JSONObject {
    if (this.error.isNotEmpty()) {
        throw Exception(this.error)
    } else {
        return JSONObject(String(this.value))
    }
}

class ClmobileVerifyException(s: String?) : IllegalStateException(s)

fun mobilecore.Result.verify(): ByteArray {
    if (this.error.isNotEmpty()) {
        throw ClmobileVerifyException(this.error)
    } else {
        return this.value
    }
}
