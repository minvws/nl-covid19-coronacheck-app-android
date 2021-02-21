package nl.rijksoverheid.ctr.api.models

import com.squareup.moshi.Moshi

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class JSON {

    fun toJson(moshi: Moshi): String {
        return moshi.adapter(javaClass).toJson(this)
    }
}
