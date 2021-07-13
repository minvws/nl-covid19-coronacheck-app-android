package nl.rijksoverheid.ctr.holder.ui.create_qr.hkvi

import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetEventFromQrUseCase {

    fun get(qrCode: String)
}

class GetEventFromQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper
) : GetEventFromQrUseCase {

    override fun get(qrCode: String) {

    }
}