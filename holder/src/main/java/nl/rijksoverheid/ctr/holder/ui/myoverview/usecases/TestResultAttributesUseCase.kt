package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface TestResultAttributesUseCase {
    fun get(credentials: String): TestResultAttributes
}

open class TestResultAttributesUseCaseImpl(
    private val moshi: Moshi,
    private val mobileCoreWrapper: MobileCoreWrapper
) : TestResultAttributesUseCase {

    override fun get(credentials: String): TestResultAttributes {
        val result = mobileCoreWrapper.readCredential(credentials.toByteArray())
        return result.decodeToString().toObject(moshi)
    }
}
