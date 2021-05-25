package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface CreateCredentialUseCase {
    fun get(secretKeyJson: String, testIsmBody: String): String
}

open class CreateCredentialUseCaseImpl(private val mobileCoreWrapper: MobileCoreWrapper) :
    CreateCredentialUseCase {

    override fun get(secretKeyJson: String, testIsmBody: String): String {
        return mobileCoreWrapper.createCredentials(
            testIsmBody.toByteArray(Charsets.UTF_8)
        )
    }
}
