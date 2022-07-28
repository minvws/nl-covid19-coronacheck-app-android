package nl.rijksoverheid.ctr.shared.ext

import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

sealed class MobileCoreException(message: String) : Exception(message) {
    class MobileCoreException1(message: String) : MobileCoreException(message)
    class MobileCoreException2(message: String) : MobileCoreException(message)
    class MobileCoreException3(message: String) : MobileCoreException(message)
    class MobileCoreException4(message: String) : MobileCoreException(message)
    class MobileCoreException5(message: String) : MobileCoreException(message)
    class MobileCoreException6(message: String) : MobileCoreException(message)
    class MobileCoreException7(message: String) : MobileCoreException(message)
    class MobileCoreException8(message: String) : MobileCoreException(message)
    class MobileCoreException9(message: String) : MobileCoreException(message)
}

fun mobilecore.Result.successString(): String {
    if (error.isNotEmpty()) {
        throw when {
            error.contains("CreateCommitmentMessage should be called before CreateCredentials") -> MobileCoreException.MobileCoreException1(error)
            error.contains("Could not unmarshal create credential messages") -> MobileCoreException.MobileCoreException2(error)
            error.contains("More credentials are being issued than there are proof builders") -> MobileCoreException.MobileCoreException3(error)
            error.contains("Proof of correctness on signature does not verify") -> MobileCoreException.MobileCoreException4(error)
            error.contains("The Signature on the attributes is not correct") -> MobileCoreException.MobileCoreException5(error)
            error.contains("Could not construct credential") -> MobileCoreException.MobileCoreException6(error)
            error.contains("Could not read freshly contructed credential") -> MobileCoreException.MobileCoreException7(error)
            error.contains("Invalid credential version in freshly constructed credential") -> MobileCoreException.MobileCoreException8(error)
            error.contains("Could not create credentials") -> MobileCoreException.MobileCoreException9(error)
            else -> Exception(this.error)
        }
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

fun initialisationException(error: String): MobileCoreException {
    return when {
        error.contains("Could not read verifier config file") -> MobileCoreException.MobileCoreException1(error)
        error.contains("Could not JSON unmarshal verifier config") -> MobileCoreException.MobileCoreException2(error)
        error.contains("The domestic verification rules were not present") -> MobileCoreException.MobileCoreException3(error)
        error.contains("The European verification rules were not present") -> MobileCoreException.MobileCoreException4(error)
        error.contains("Could not load public keys config") -> MobileCoreException.MobileCoreException5(error)
        else -> MobileCoreException.MobileCoreException6("Unknown error")
    }
}

fun mobilecore.Result.verify(): ByteArray {
    if (this.error.isNotEmpty()) {
        throw ClmobileVerifyException(this.error)
    } else {
        return this.value
    }
}
