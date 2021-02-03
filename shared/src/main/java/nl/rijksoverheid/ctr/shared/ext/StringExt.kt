package nl.rijksoverheid.ctr.shared.ext

import com.squareup.moshi.Moshi

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
inline fun <reified O> String.toObject(moshi: Moshi): O {
    return moshi.adapter(O::class.java).fromJson(this) ?: throw Exception("Failed to create object from json string")
}
