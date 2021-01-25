package nl.rijksoverheid.ctr.usecases

import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.data.models.EventQr
import nl.rijksoverheid.ctr.data.models.Issuers

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IsEventQrValidUseCase(
    private val isSignatureValidUseCase: IsSignatureValidUseCase,
    private val eventUtil: EventUtil
) {

    sealed class EventQrValidResult {
        object Valid : EventQrValidResult()
        class Invalid(val reason: String) : EventQrValidResult()
    }

    fun isValid(issuers: List<Issuers.Issuer>, eventQR: EventQr): EventQrValidResult {
        val event = eventQR.event

//        val eventSignatureValid = isSignatureValidUseCase.isValid(
//            issuers,
//            eventQR.eventSignature,
//            event
//        )
//
//        if (!eventSignatureValid) {
//            return EventQrValidResult.Invalid("Event Signature Invalid")
//        }

        if (!eventUtil.timeValid(event)) {
            return EventQrValidResult.Invalid("Event Time Invalid")
        }

        return EventQrValidResult.Valid
    }
}
