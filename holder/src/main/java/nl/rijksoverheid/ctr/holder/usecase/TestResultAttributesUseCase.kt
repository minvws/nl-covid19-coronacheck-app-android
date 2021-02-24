package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.api.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class TestResultAttributesUseCase(
    private val moshi: Moshi,
) {

    open fun get(credentials: String): TestResultAttributes {
        val result = Clmobile.readCredential(credentials.toByteArray()).verify()
        return result.decodeToString().toObject(moshi)
    }
}
