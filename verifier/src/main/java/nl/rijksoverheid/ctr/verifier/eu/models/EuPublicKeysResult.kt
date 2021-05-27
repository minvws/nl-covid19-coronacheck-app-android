/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.eu.models

import okhttp3.ResponseBody

sealed class EuPublicKeysResult {
    data class Success(val publicKeys: ResponseBody) : EuPublicKeysResult()
    object Error : EuPublicKeysResult()
}