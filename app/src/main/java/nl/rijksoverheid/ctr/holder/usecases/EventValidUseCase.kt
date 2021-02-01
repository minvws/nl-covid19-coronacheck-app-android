package nl.rijksoverheid.ctr.holder.usecases

import nl.rijksoverheid.ctr.holder.util.EventUtil
import nl.rijksoverheid.ctr.shared.models.Issuers
import nl.rijksoverheid.ctr.shared.models.RemoteEvent
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class EventValidUseCase(
    private val isSignatureValidUseCase: SignatureValidUseCase,
    private val eventUtil: EventUtil
) {

    fun checkValid(issuers: List<Issuers.Issuer>, remoteEvent: RemoteEvent) {
        val event = remoteEvent.event

        //TODO: Does not yet work because payload json expects forward slashes
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
            throw Exception("Event time invalid")
        }
    }
}
